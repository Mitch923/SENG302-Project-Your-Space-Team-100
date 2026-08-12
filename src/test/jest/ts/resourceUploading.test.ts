import {Object3D} from "three";
import {getPreviewModelLoader} from "../../../main/ts/previewScene";
import * as resourceUploading from "@/util/resourceUploading";
import {fileToTexture} from "../../../main/ts/util/fileToTexture";

jest.mock("bootstrap");

jest.mock("../../../main/ts/previewScene", () => ({
    getPreviewModelLoader: jest.fn()
}));

jest.mock("@/util/fileToTexture", () => ({
    fileToTexture: jest.fn()
}))

jest.mock("@/objectcontrols/objectManipulator", () => ({
    setObjectTexture: jest.fn()
}))


describe("addParallelTexture", () => {
    const mockGetMostRecentlyLoadedModel = jest.fn();
    const mockSetMostRecentlyLoadedParallelTexture = jest.fn();
    const mockLoader = {
        getMostRecentlyLoadedModel: mockGetMostRecentlyLoadedModel,
        setMostRecentlyLoadedParallelTexture: mockSetMostRecentlyLoadedParallelTexture
    };


    beforeEach(() => {
        (getPreviewModelLoader as jest.Mock).mockReturnValue(mockLoader);

        const mockInputElement = {
            value: "",
            files: [new File(["test file"], "test.png", {type: "image/png"})],
            addEventListener: jest.fn(),
            removeEventListener: jest.fn(),
            click: jest.fn(),
            focus: jest.fn(),
        } as unknown as HTMLInputElement;

        jest.spyOn(document, "getElementById").mockReturnValue(mockInputElement);
    });

    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("Applies the listener and converts", () => {
        const mockObject3D = new Object3D();
        mockGetMostRecentlyLoadedModel.mockReturnValue(mockObject3D);
        resourceUploading.applyParallelTexture()

        expect(document.getElementById).toHaveBeenCalledTimes(1);
        expect(fileToTexture).toHaveBeenCalledTimes(1)
    });
});
