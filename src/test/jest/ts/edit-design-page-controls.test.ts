import {onTextureSelected} from "../../../main/ts/edit-design-page-controls";
import * as ToastNotifications from "../../../main/ts/util/toastNotifications";
import {showFailToast} from "../../../main/ts/util/toastNotifications";

let textureInputElement = document.createElement("input");
let textureContainer = document.createElement("div");

jest.spyOn(document, 'getElementById').mockImplementation((id) => {
    if (id === 'textureInput') return textureInputElement;
    if (id === 'custom-texture-container') return textureContainer;
    return null;
});

describe("onTextureSelected", () => {
    beforeEach(() => {
        textureInputElement = document.createElement("input");
        const file = new File(["test file"], "test.png", {type: "image/png"});
        Object.defineProperty(textureInputElement, "files", {
            value: [file],
            writable: false,
        });

        global.fetch = jest.fn().mockResolvedValue({
            status: 200,
            ok: true,
            text: jest.fn().mockResolvedValue({}),
            json: jest.fn().mockResolvedValue({})
        });
    });

    describe("wrong file type", () => {
        let showToastSpy: jest.SpyInstance

        beforeEach(() => {
            showToastSpy = jest.spyOn(ToastNotifications, 'showFailToast').mockImplementation()
        })

        it("should reject files with type svg", () => {
            global.fetch = jest.fn().mockResolvedValue({
                status: 400,
                ok: false,
                body: JSON.stringify({imageUpload: "Texture File must be of type png or jpg"}),
                json: jest.fn().mockResolvedValue({imageUpload: "Texture File must be of type png or jpg"})
            });

            onTextureSelected(new Event("mock event"));

            expect(fetch).toHaveBeenCalledTimes(1);
            expect(fetch).toHaveBeenCalledWith(
                    "uploadTexture", {
                        method: "POST",
                        body: expect.any(FormData),
                        headers: {
                            ["MockCSRFHeaderName"]: "MockCSRFToken",
                        }
                    });
        })
        afterEach(() => {
            expect(showToastSpy).toHaveBeenCalledTimes(1);
            expect(showToastSpy).toHaveBeenCalledWith("Texture File must be of type png or jpg")
            showToastSpy.mockReset();
        })
    })

    describe("file too large", () => {
        let showToastSpy: jest.SpyInstance

        beforeEach(() => {
            showToastSpy = jest.spyOn(ToastNotifications, 'showFailToast').mockImplementation()
            textureInputElement = document.createElement("input");
            const file = new File([new Uint8Array(10 * 1024 * 1024 + 1)], "test.png", {type: "image/png"});
            Object.defineProperty(textureInputElement, "files", {
                value: [file],
                writable: false,
            });
        })

        it("should reject files that are too large", () => {
            onTextureSelected(new Event("mock event"));
        })
        afterEach(() => {
            expect(showToastSpy).toHaveBeenCalledWith("File upload must be less than 10MB")
            showToastSpy.mockReset();
        })
    })

    it("calls fetch with the correct form data", async () => {
        await onTextureSelected(new Event("mock event"));

        expect(fetch).toHaveBeenCalledTimes(1);
        expect(fetch).toHaveBeenCalledWith(
                "uploadTexture", {
                    method: "POST",
                    body: expect.any(FormData),
                    headers: {
                        ["MockCSRFHeaderName"]: "MockCSRFToken",
                    }
                });

        const formData = (fetch as jest.Mock).mock.calls[0][1].body as FormData;
        const file = formData.get("file") as File;
        expect(file.name).toBe("test.png");
        expect(file.type).toBe("image/png");
    })

    it("doesn't call fetch if file is null/empty", async () => {
        textureInputElement = document.createElement("input");
        await onTextureSelected(new Event("mock event"));
        expect(fetch).toHaveBeenCalledTimes(0);
    })
})