import CSRFProvider from "./util/csrfProvider";
import {showFailToast, showSuccessToast} from "./util/toastNotifications";
import {WebGLRenderer} from "three";
import {saveScreenshot, ScreenShotParams, takeScreenshot} from "./util/renderer-screenshot";
import {EditorScene} from "./editor-scene";
import {DesignDataProvider} from "@/util/designDataProvider";
import {UploadConfig, UploadManager} from "@/chunking/uploading/UploadManager";
import {FileChunker} from "@/chunking/FileChunker";
import {StaticSizeStrategy} from "@/chunking/strategies/StaticSizeStrategy";
import {updateSaveProgress} from "@/edit-design-page-controls";

/**
 * Represents the input values and CSRF info required for saving a design.
 *
 *  @property name - The name of the design.
 *  @property description - The description of the design.
 *  @property csrfHeaderName - The name of the CSRF header for security.
 *  @property csrfToken - The CSRF token to include in the request.
 *  @property renovationId - The ID of the renovation record the design belongs to.
 *  @property designId - The ID of the design being saved.
 */
interface DesignFormValues {
    name: string;
    description: string;
    csrfHeaderName: string;
    csrfToken: string;
    renovationId: string;
    designId: string;
    designRoomId: string;
}

/**
 * Represents the context of the design form, including DOM elements, values, and validation error targets.
 *
 * @property elements - References to form html input fields.
 * @property values - The extracted values from the form.
 * @property errorElements - Elements used to display validation errors.
 */
interface DesignFormContext {
    elements: {
        designNameField: HTMLInputElement;
        designDescField: HTMLInputElement;
    }
    values: DesignFormValues,
    errorElements: {
        designNameError: HTMLElement;
    }
}

/**
 * Extracts the design form fields and their current values.
 *
 * @returns {DesignFormContext | null} An object containing field elements and values, or null if required DOM elements are missing.
 */
function getFormContext(): DesignFormContext | null {
    const designName = document.getElementById("designName") as HTMLInputElement | null;
    const designDescription = document.getElementById("designDescription") as HTMLInputElement | null;
    const renovationIdInput = document.getElementById("renovationId") as HTMLInputElement | null;
    const designIdInput = document.getElementById("designId") as HTMLInputElement | null;
    const designNameError = document.getElementById("designNameError") as HTMLInputElement | null;
    const designRoomInput = document.getElementById("designRoom") as HTMLInputElement;

    if (!designName || !designDescription || !renovationIdInput || !designIdInput || !designNameError || (!designRoomInput && DesignDataProvider.isRenovationDesign())) {
        console.error("Missing required elements in the DOM.");
        return null;
    }

    const name = designName.value.trim();
    const description = designDescription.value.trim();

    const csrfToken = CSRFProvider.getCsrfToken();
    const csrfHeaderName = CSRFProvider.getCsrfHeaderName();

    const renovationId = renovationIdInput.value;
    const designId = designIdInput.value;

    let designRoomId;
    if (!designRoomInput) {
        designRoomId = "-1";
    } else {
        designRoomId = designRoomInput.value;
    }

    return {
        elements: {
            designNameField: designName,
            designDescField: designDescription
        },
        values: {
            name,
            description,
            csrfHeaderName,
            csrfToken,
            renovationId,
            designId,
            designRoomId
        },
        errorElements: {
            designNameError: designNameError
        }
    }
}

/**
 * Main function to handle saving a design.
 *
 * Validates the form, exports the current scene to GLB binary format, constructs a FormData payload,
 * and submits it via fetch to the API endpoint.
 *
 * @returns {Promise<void>} A promise that resolves once the save process completes or errors.
 */
export async function saveDesign(
        gltfBinary: ArrayBuffer,
        renderer: WebGLRenderer,
        scene: EditorScene,
): Promise<void> {

    // extract details we need to send to server
    const values = getFormContext()?.values;
    if (values === undefined) {
        showFailToast("An error occurred while saving your design, please try again");
        throw new Error("Expected form values to exist, but they don't");
    }

    try {
        await saveChunkyDesignData(gltfBinary, {
            designId: parseInt(values.designId, 10),
            isCompetition: !DesignDataProvider.isRenovationDesign()
        });
        await saveDesignDetails(values);
        await saveDesignScreenShot(renderer, scene, values.designId);

    } catch (e) {
        console.error("Oh no", e)
    }
}

/**
 * Save the other details of a design (name, description, room).
 *
 * @param values DesignFormValues
 */
async function saveDesignDetails(values: DesignFormValues) {
    const {
        name,
        description,
        csrfHeaderName,
        csrfToken,
        designId,
        renovationId,
        designRoomId
    } = values;

    // construct url
    let url;
    if (DesignDataProvider.isRenovationDesign()) {
        url = `renovationRecord/${renovationId}/saveDesign/${designId}`;
    } else {
        url = `editCompetitionEntry/${designId}`;
    }

    const formData = new FormData();
    formData.append("json", new Blob(
            [JSON.stringify({name, description, designRoomId})],
            {type: "application/json"}
    ));

    return fetch(url, {
        method: "POST",
        body: formData,
        headers: {
            [csrfHeaderName]: csrfToken
        }
    });
}

/**
 * Take and save the screenshot of a scene to be the designs thumbnail image.
 *
 * @param renderer
 * @param scene
 * @param designId
 */
async function saveDesignScreenShot(renderer: WebGLRenderer, scene: EditorScene, designId: string) {
    const params: ScreenShotParams = {
        renderer: renderer,
        scene: scene,
        imageType: 'jpeg',
        quality: 0.8,
    };

    const screenShotBlob = await takeScreenshot(params);

    let url;
    if (DesignDataProvider.isRenovationDesign()) {
        url = `upload/image/design/${parseInt(designId, 10)}`;
    } else {
        url = `upload/image/competitionEntry/${parseInt(designId, 10)}`;
    }

    if (screenShotBlob) {
        await saveScreenshot(screenShotBlob, url);
    } else {
        console.warn("Failed to capture screenshot");
    }
}

/**
 * Save the scene data using new chunking method.
 *
 * @param gltfBinary
 * @param config
 */
async function saveChunkyDesignData(gltfBinary: ArrayBuffer, config: UploadConfig): Promise<void> {
    const chunkingStrategy = new StaticSizeStrategy(9);
    const fileChunker = new FileChunker(chunkingStrategy);
    const uploadManager = new UploadManager(fileChunker);
    uploadManager.onProgress = (progress) => updateSaveProgress(progress);
    await uploadManager.uploadFile(new Blob([gltfBinary]), config);
}
