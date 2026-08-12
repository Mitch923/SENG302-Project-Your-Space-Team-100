import {EditorScene} from "@/editor-scene";
import {ModelCounter} from "./ModelCounter";
import {
    applyModelCardEventListeners,
    insertUploadedModel,
    PBRTextures
} from "@/edit-design-page-controls";
import {Modal} from "bootstrap";
import {getPreviewModelLoader, initPreview, takeModelScreenshot, uploadModel} from "@/previewScene";
import CSRFProvider from "./csrfProvider";
import {
    validateFileSize,
    validateModelFileType,
    validateModelUploadNameLength,
    validateModelUploadNameNotEmpty
} from "@/util/uploadValidation";
import {showFailToast} from "@/util/toastNotifications";
import {Mesh, MeshStandardMaterial, Object3D, Texture} from "three";
import {validateParallelTextureImage} from "@/util/validation";
import {fileToTexture} from "@/util/fileToTexture";

/**
 * Utility function to upload a resource and any extra
 * "query parameters" (i.e. form data key value pairs)
 * to a specific endpoint url using a POST request.
 * @param url - the url you want to POST the data to
 * @param fileInfo { paramName: string; file: File } - paramName is the name of the parameter that will be associated with the file in the formdata object that is sent, file is the file object you want to send
 * @param extraParams - extra key: value pairs of data you want to POST within the form data being uploaded to the server
 * @param csrfToken - the csrfToken from the html inserted by spring (usually via a hidden input)
 * @param csrfHeader - the csrfHeader from the html inserted by spring needed for making the POST
 */
export async function uploadResource(
        url: string,
        fileInfo: { paramName: string; file: File },
        extraParams: { [key: string]: any }, // an indefinite amount of key: value pairs, where the key is a string and the value can be of any type.
        csrfToken: string,
        csrfHeader: string
) {

    const formData = new FormData();

    // append the file's parameter name and the file contents
    formData.append(fileInfo.paramName, fileInfo.file);

    // append all the extra parameters and their values to the form data
    Object.entries(extraParams).forEach(([key, value]) => {
        formData.append(key, value);
    });

    try {
        const response = await fetch(url, {
            method: 'POST',
            body: formData,
            headers: {
                [csrfHeader]: csrfToken // attach CSRF token
            },
        });

        if (!response.ok) {
            console.error(await response.text());
        }

        return response;

    } catch (err) {
        console.error(err);
        alert('An error occurred while uploading the resource');
    }
}

/**
 * A setup function that grabs all the associated html elements for model uploading,
 * adds the necessary listeners, and defines what should happen when a model gets uploaded
 * via the "upload model" button.
 */
export function setUpModelUploading(scene: EditorScene, modelCounter: ModelCounter) {
    const fileInput = document.getElementById('model-upload-input') as HTMLInputElement;
    const uploadButton = document.getElementById("model-upload-btn") as HTMLButtonElement;

    // CSRF tokens
    const csrfToken = CSRFProvider.getCsrfToken();
    const csrfHeaderName = CSRFProvider.getCsrfHeaderName();

    // userId from hidden input
    const userIdInput = document.getElementById('userId') as HTMLInputElement;
    const userId = userIdInput.value;

    // Triggering file picker
    uploadButton.addEventListener('click', () => fileInput.click());

    // Triggering model upload from preview modal
    document.getElementById('uploadButton')?.addEventListener('click', async e => {
        const displayNameElement = document.getElementById("modelName") as HTMLInputElement;
        displayNameElement.classList.remove('is-invalid')

        // validate the name field
        if (!validateModelUploadNameNotEmpty(displayNameElement.value)) {
            showFailToast("Model name cannot be empty");
            displayNameElement.classList.add('is-invalid');
            return;
        }

        if (!validateModelUploadNameLength(displayNameElement.value)) {
            showFailToast("Model name must be 32 characters or less");
            displayNameElement.classList.add('is-invalid');
            return;
        }

        // getPreviewModelLoader().swapToUntexturedModel()
        const modelGLB = await uploadModel();
        // getPreviewModelLoader().swapToTexturedModel()
        const displayName = displayNameElement.value;
        if (modelGLB !== undefined) {
            const uploadResponse = await uploadResource(
                    modelUploadUrl,
                    {
                        paramName: "modelGLB",
                        file: modelGLB
                    },
                    {"displayName": displayName},
                    csrfToken,
                    csrfHeaderName
            );

            if (uploadResponse === undefined) {
                throw new Error("ERROR: upload response undefined");
            }

            if (!uploadResponse.ok) {
                throw new Error("ERROR: upload response not ok");
            }

            const modelId = parseInt(await uploadResponse.text());

            // Save the screenshot image for new model
            await takeModelScreenshot(modelId);

            // Save the parallel texture for the model if one has been uploaded
            const parallelTextureFile = getPreviewModelLoader().getMostRecentlyLoadedParallelTexture();
            if (parallelTextureFile) {
                const textureUploadUrl = `upload/parallelTexture/${modelId}`;
                await uploadResource(
                        textureUploadUrl,
                        {
                            paramName: "parallelTexture",
                            file: parallelTextureFile
                        },
                        {},
                        csrfToken,
                        csrfHeaderName
                );
            }

            // insert new model card so that the uploaded models tab includes the newly uploaded model
            await insertUploadedModel(modelId);
            applyModelCardEventListeners(scene, modelCounter);

            // Reset input so the same file can be uploaded again
            fileInput.value = '';

            if (previewModalChange) {
                previewModalChange.hide();
            }
        }
    });

    addParallelTextureEventListener()

    // create model upload url
    const modelUploadUrl = `upload/model/${userId}`;

    const previewModal = document.getElementById('previewModal') as HTMLElement;
    const previewModalChange = Modal.getOrCreateInstance(previewModal);

    // Handling file selection
    fileInput.addEventListener('change', async () => {
        if (!fileInput.files || fileInput.files.length === 0) return;

        const file: File = fileInput.files[0];

        // validate file extension
        if (!validateModelFileType(file)) {
            showFailToast("Model file is of invalid type.");
            fileInput.value = '';
            return;
        }

        // validate file size

        if (!validateFileSize(file)) {
            showFailToast("File upload must be less than 10MB");
            fileInput.value = '';
            return;
        }

        // file size is validated, show preview modal before upload

        if (previewModalChange) {
            previewModalChange.show();
            await initPreview(fileInput.files[0])
        }

    });
}

export async function applyParallelTexture() {
    const parallelTextureInput = document.getElementById("parallelTextureInput") as HTMLInputElement;
    if (!parallelTextureInput.files || !parallelTextureInput.files.length) return;
    const textureFile: File = parallelTextureInput.files[0];
    if (!validateParallelTextureImage(textureFile)) return;

    const texture: Texture = await fileToTexture(textureFile);
    const pbrTextures: PBRTextures = {
        aoMap: null,
        map: texture,
        displacementMap: null,
        normalMap: null,
        roughnessMap: null
    }

    const uploadedModel: Object3D | null = getPreviewModelLoader().getMostRecentlyLoadedModel();
    if (!uploadedModel) return;

    uploadedModel.traverse((child) => {
        if (child instanceof Mesh) {
            const materials = Array.isArray(child.material) ? child.material : [child.material];

            materials.forEach((mat: MeshStandardMaterial) => {
                if (!mat.userData.originalMap) {
                    // Store the original map the first time
                    mat.userData.originalMap = mat.map || null;
                    mat.userData.isParallelTexture = true; // flag that this is a parallel texture
                }

                // Apply the parallel texture
                mat.map = pbrTextures.map;
                mat.needsUpdate = true;
            });
        }
    });

    getPreviewModelLoader().setMostRecentlyLoadedParallelTexture(textureFile);
}

/**
 * Applies a listener to the parallel texture input that converts the file given into a PBRTextures
 * object and applies it to the object if it is valid.
 */
function addParallelTextureEventListener() {
    document.getElementById("parallelTextureInput")?.addEventListener("change", async e => {
        await applyParallelTexture()
    })
}