import {
    saveScreenshot,
    ScreenShotParams,
    takeScreenshot
} from "../../../main/ts/util/renderer-screenshot"
import CSRFProvider from "@/util/csrfProvider";
import {PerspectiveCamera, Vector3, WebGLRenderer} from "three";

const mockTransformControls = {
    getHelper: jest.fn().mockReturnValue({visible: true}),
};

const mockEditorScene = {
    disableGrid: jest.fn(),
    enableGrid: jest.fn(),
    transformControls: mockTransformControls,
};

jest.mock('three', () => {
    const actualThree = jest.requireActual('three');
    const PerspectiveCamera = jest.fn().mockImplementation(
            () => ({
                position: {copy: jest.fn().mockReturnThis()} as any,
                lookAt: jest.fn(),
                updateProjectionMatrix: jest.fn(),
                aspect: 1,
            })
    );

    const Vector3 = jest.fn().mockImplementation(
            (x, y, z) => ({
                x, y, z,
                clone: jest.fn().mockReturnThis(),
            }));

    const WebGLRenderer = jest.fn().mockImplementation(
            () => {
                const domElement = {
                    width: 1920,
                    height: 1080,
                    toBlob: jest.fn((callback, type) =>
                            callback(new Blob(['mocked-blob'], {type: type ?? 'image/jpeg'}))
                    ),
                    getContext: jest.fn().mockReturnValue({}),
                };

                return {
                    domElement,
                    setSize: jest.fn((width: number, height: number, updateStyle?: boolean) => {
                        domElement.width = width;
                        domElement.height = height;
                    }),
                    setPixelRatio: jest.fn(),
                    getPixelRatio: jest.fn(() => 1),
                    render: jest.fn(),
                    getSize: jest.fn().mockReturnValue(new actualThree.Vector2(domElement.width, domElement.height)),
                }
            }
    )
    return {
        ...actualThree,
        PerspectiveCamera,
        WebGLRenderer,
        Vector3,
    }
})

describe('takeScreenshot', () => {
    let renderer: WebGLRenderer;
    let params: ScreenShotParams;

    beforeEach(() => {
        jest.clearAllMocks();
        renderer = new WebGLRenderer();
        params = {
            renderer,
            scene: mockEditorScene as any,
            imageType: 'jpeg',
            quality: 0.8,
            width: 800,
            height: 600,
            cameraPosition: new Vector3(10, 10, 10),
            cameraTarget: new Vector3(0, 0, 0),
        };
    });

    it('should create the screenshot and return blob', async () => {
        const blob = await takeScreenshot(params);

        expect(PerspectiveCamera).toHaveBeenCalled();
        expect(params.renderer.setSize).toHaveBeenCalledWith(params.width, params.height, false);
        expect(params.renderer.setPixelRatio).toHaveBeenCalledWith(1);
        expect(mockEditorScene.disableGrid).toHaveBeenCalled();
        expect(params.renderer.getSize).toHaveBeenCalled();
        // Should return transform controls to original value
        expect(mockEditorScene.transformControls.getHelper().visible).toBe(true);
        expect(params.renderer.render).toHaveBeenCalledWith(mockEditorScene, expect.anything());
        expect(params.renderer.domElement.toBlob).toHaveBeenCalledWith(
                expect.any(Function),
                'image/jpeg',
                params.quality,
        );
        expect(blob).toBeInstanceOf(Blob);
        expect(mockEditorScene.enableGrid).toHaveBeenCalled();
    });

    it('should use default width, height, and vectors if not provided', async () => {
        const minimalParams: ScreenShotParams = {
            renderer,
            scene: mockEditorScene as any,
            imageType: 'png',
        }

        await takeScreenshot(minimalParams);

        expect(minimalParams.renderer.setSize).toHaveBeenCalledWith(800, 600, false);
        expect(Vector3).toHaveBeenCalledWith(15, 15, 15); // Camera position default
        expect(Vector3).toHaveBeenCalledWith(0, 0, 0); // Camera target default
        expect(minimalParams.renderer.domElement.toBlob).toHaveBeenCalledWith(
                expect.any(Function),
                'image/png',
                undefined // Quality was not defined
        );
    });

    it('should restore original settings on error', async () => {
        const error = new Error('Render Failed');
        (renderer.render as jest.Mock).mockImplementation(() => {
            throw error;
        });
        const beforeWidth = renderer.domElement.width;
        const beforeHeight = renderer.domElement.height;

        await expect(takeScreenshot(params)).rejects.toThrow(error);

        expect(mockEditorScene.enableGrid).toHaveBeenCalled();
        expect(mockEditorScene.transformControls.getHelper().visible).toBe(true);
        expect(renderer.setSize).toHaveBeenCalledWith(beforeWidth, beforeHeight, false);
        expect(renderer.setPixelRatio).toHaveBeenCalledWith(1);
    });
});

describe('saveScreenshot', () => {
    beforeEach(() => {
        jest.clearAllMocks();
        global.fetch = jest.fn();
    });

    it('should send a screenshot to the server', async () => {
        (global.fetch as jest.Mock).mockResolvedValue({
            ok: true,
            text: jest.fn().mockResolvedValue('Success'),
        });

        const blob = new Blob(['test-blob'], {type: 'image/jpeg'});
        await saveScreenshot(blob, "upload/image/design/123");

        expect(global.fetch).toHaveBeenCalledWith(
                'upload/image/design/123',
                {
                    method: 'POST',
                    headers: {
                        "MockCSRFHeaderName": "MockCSRFToken",
                    },
                    body: expect.any(FormData),
                }
        );
        expect(CSRFProvider.getCsrfHeaderName).toHaveBeenCalled();
        expect(CSRFProvider.getCsrfToken).toHaveBeenCalled();
    });

    it('should log upload failure', async () => {
        const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();
        (global.fetch as jest.Mock).mockResolvedValue({
            ok: false,
            text: jest.fn().mockResolvedValue('Fail'),
        });

        const blob = new Blob(['test-blob'], {type: 'image/jpeg'});
        await saveScreenshot(blob, "upload/image/design/123");

        expect(console.error).toHaveBeenCalledWith('Upload failed:', 'Fail');
        consoleErrorSpy.mockRestore();
    });
});