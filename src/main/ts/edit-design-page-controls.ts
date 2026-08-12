import {validateDesignDescription, validateDesignName} from "./util/validation";
import {saveDesign} from "./designSaving";
import {GLTFExportHandler} from "./util/gltfHandlers";
import {DesignDataProvider} from "./util/designDataProvider";
import CSRFProvider from "@/util/csrfProvider";
import {showFailToast, showSuccessToast} from "./util/toastNotifications";
import {ModelLoader} from "./ModelLoader";
import {Scene, SRGBColorSpace, Texture, TextureLoader, WebGLRenderer} from "three";
import {ModelCounter} from "./util/ModelCounter";
import {setObjectTexture} from "./objectcontrols/objectManipulator";
import {SceneObjectSelector} from "./objectcontrols/SceneObjectSelector";
import {EditorScene} from "./editor-scene";
import {UploadProgress} from "@/chunking/uploading/UploadManager";

// Map in the form ["htmlElementID", function]
// This maps an htmlElement to an onclick listener to be applied to it
const onclickControlsMap = new Map<string, (arg0: MouseEvent) => void>([
    ["backModalContinue", returnToRenovationRecord],
    ["deleteDesignButton", deleteDesign],
    ["publicDesignBackButton", returnToRenovationRecord]
]);

/**
 * unload handler to ensure user doesn't leave without unsaved changes
 * @param event
 */
export const beforeUnloadHandler = (event: BeforeUnloadEvent) => {
    event.preventDefault();
    return 'You have unsaved changes. Are you sure you want to leave?';
}

/**
 * Returns to the view renovation record page associated with the currently edited design.
 */
function returnToRenovationRecord() {
    const renovationRecordIdInput: HTMLInputElement = document.getElementById("renovationId") as HTMLInputElement;
    const renovationRecordId: number = parseInt(renovationRecordIdInput.value, 10);
    const designIdInput: HTMLInputElement = document.getElementById("designId") as HTMLInputElement;
    const designId: number = parseInt(designIdInput.value, 10);

    // User is returning voluntarily so remove extra dialog.
    window.removeEventListener('beforeunload', beforeUnloadHandler);

    if (!isNaN(renovationRecordId)) {
        // Go to associated renovation
        window.location.assign(`viewRenovation/${renovationRecordId}?designId=${designId}`);
    } else {
        history.back(); // Default to last page
    }
}

/**
 * Iterates through the controls map and applies the onclick listener in the map to each element specified by the maps key
 *
 */
export function applyButtonEventListeners(
        saveHandler: GLTFExportHandler,
        renderer: WebGLRenderer,
        scene: EditorScene,
) {
    onclickControlsMap.set("saveDesign", createOnSubmitDesign(saveHandler, renderer, scene));

    if (DesignDataProvider.isOwned()) {
        window.addEventListener("beforeunload", beforeUnloadHandler);
    }

    onclickControlsMap.forEach((callback, id) => {
        const targetElement = document.getElementById(id);
        if (targetElement) {
            targetElement.addEventListener('click', callback);
        } else {
            console.warn(`Cannot apply onclick listener to element with id: ${id}, Element is ${targetElement}`);
        }
    });
    const textureInput = document.getElementById("textureInput") as HTMLInputElement;
    if (textureInput) {
        textureInput.addEventListener("change", onTextureSelected);
    }
}

export async function onTextureSelected(e: Event) {
    const textureInput = document.getElementById("textureInput") as HTMLInputElement;
    if (!textureInput.files || !textureInput.files.length) return;

    const textureFile = textureInput.files[0];

    if (textureFile.size / 1024 / 1024 <= 10) {
        try {
            const csrfHeader = CSRFProvider.getCsrfHeaderName();
            const csrfToken = CSRFProvider.getCsrfToken();

            const formData = new FormData();

            formData.append("file", textureFile);

            fetch("uploadTexture", {
                method: "POST",
                body: formData,
                headers: {
                    [csrfHeader]: csrfToken,
                }
            }).then(async (res) => {
                res.json().then(async data => {
                    if (res.status == 400) {
                        showFailToast(data.imageUpload)
                    } else {
                        const textureId = parseInt(data.textureID);
                        await insertTextureCard(textureId);
                    }
                });
            });
        } catch (e) {
            console.error(`error while uploading texture file`);
        }
    } else {
        showFailToast("File upload must be less than 10MB")
    }
}

/**
 *
 * @param id
 */
async function insertTextureCard(id: number) {
    const url = `fragment/texture/${id}`;
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`Failed to fetch texture fragment with id: ${id}`)
    }
    const customTextureTab = document.getElementById("custom-texture-container") as HTMLElement;
    customTextureTab.insertAdjacentHTML("beforeend", await response.text());
    const newElement = customTextureTab.lastElementChild;
    if (newElement) {
        applyTextureCardEventListeners();
    }
}

/**
 * Set up the confirmation model for deleting a custom model.
 */
export function setupDeleteModal() {
    // Setup deletion modal
    const deleteModelModalElement = document.getElementById('deleteModelModal') as HTMLElement;
    const deleteModelModalButton = document.getElementById('deleteModelModalButton') as HTMLButtonElement;

    // Annoying hack to avoid ts-ignore as we dont have correct bootstrap types
    interface ModalEvent extends Event {
        relatedTarget?: HTMLElement;
    }

    let currentModelId: number | null = null;
    let currentType: string | null = null;

    let deleteModelHandler = () => {
        const modelId = currentModelId?.toFixed(0);
        console.log(`Handler called with id ${modelId}`)
        if (currentModelId && currentType == "model") {
            deleteCustomModel(Number(modelId)).then();
        } else if (currentModelId && currentType == "texture") {
            deleteCustomTexture(Number(modelId)).then();
        }
    };

    deleteModelModalButton.addEventListener('click', deleteModelHandler);

    deleteModelModalElement.addEventListener('show.bs.modal', (event: ModalEvent) => {
        const button = event.relatedTarget as HTMLButtonElement;

        // Grab info from related button
        currentType = button.getAttribute('data-bs-type');
        currentModelId = Number(button.getAttribute('data-bs-id'));

        const modalTextElement = document.getElementById('deleteModelModalText') as HTMLParagraphElement;

        modalTextElement.innerText = `Are you sure you want to delete this custom ${currentType === "model" ? "model" : "texture"}? This cannot be undone.`;
    });
}

/**
 * Iterates over all the model cards in the object library and applies onclick listener to add the model to the scene.
 * Also initialise the modal for deleting custom models.
 * @param scene scene the objects should be loaded into.
 * @param modelCounter - the model counter instance to keep track of when models are added
 */
export function applyModelCardEventListeners(scene: Scene, modelCounter: ModelCounter) {
    const modelLoader = new ModelLoader(scene);
    document.querySelectorAll('.add-model-btn').forEach((el) => {
        let element = el as HTMLElement;
        if (element.dataset.type == "model") {
            const triggerLoad = () => {
                const path = element.dataset.filepath;
                const modelName = element.dataset.modelName;
                const textureFilePath = element.dataset.parallelTexturePath;
                if (modelCounter.getObjectCount() >= 50) {
                    showFailToast("You cannot add more than 50 objects to a design.");
                } else if (path && modelName) {
                    modelLoader.loadModel(path, modelName, textureFilePath).then(r => modelCounter.incrementObjectCount());
                }
            };

            const newElement = element.cloneNode(true);
            // @ts-ignore
            element.parentNode.replaceChild(newElement, element);
            element = newElement as HTMLElement;

            element.addEventListener('click', triggerLoad);
            element.addEventListener('keydown', (event) => {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault(); // prevent scrolling for spacebar
                    triggerLoad();
                }
            });
        }
    });
}

/**
 * Contains data for a set of textures that make up a PBR texture.
 * Includes in order, ambient occlusion, color, displacement, normal, roughness
 */
export interface PBRTextures {
    aoMap: Texture | null;
    map: Texture | null;
    displacementMap: Texture | null;
    normalMap: Texture | null;
    roughnessMap: Texture | null;
}

/**
 * Adds all models that weren't uploaded by a specific user to the scene
 */
export async function insertAllPublicModels() {
    const publicIds = await getPublicModelIds();
    for (let id of publicIds) {
        await insertPublicModel(id);
    }
}

/**
 * Makes a request to the backend for the ids of all models that don't belong to a user
 */
export async function getPublicModelIds(): Promise<number[]> {
    const response = await fetch('ids/model/public');

    if (!response.ok) {
        throw new Error('Failed to fetch public model IDs');
    }

    return await response.json();
}

/**
 * Adds all the public models to the public models tab mas cards.
 * @param id id of the model
 */
export async function insertPublicModel(id: number) {
    const url = `fragment/model/${id}`;

    const modelTab = document.getElementById("public-models-tab") as HTMLElement;

    const modelFragmentResponse = await fetch(url);

    if (!modelFragmentResponse.ok) {
        throw new Error(`Failed to fetch model fragment for id ${id}`);
    }

    modelTab.insertAdjacentHTML("beforeend", await modelFragmentResponse.text());
}

/**
 * Adds all models that were uploaded bvy the logged in user
 */
export async function insertAllUploadedModels() {
    const uploadedIds = await getUploadedModelIds();
    for (let id of uploadedIds) {
        await insertUploadedModel(id);
    }
}

/**
 * Makes a request to the backend for the ids of all models that were uploaded byt the logged in user
 */
export async function getUploadedModelIds(): Promise<number[]> {
    const response = await fetch('ids/model/uploaded');

    if (!response.ok) {
        throw new Error('Failed to fetch public model IDs');
    }

    return await response.json();
}

/**
 * Adds all the models the logged-in user has uploaded in the past to the uploaded models tab.
 * @param id id of the model
 */
export async function insertUploadedModel(id: number) {
    const url = `fragment/model/${id}`;

    const modelTab = document.getElementById("uploaded-models-tab") as HTMLElement;

    const modelFragmentResponse = await fetch(url);

    if (!modelFragmentResponse.ok) {
        throw new Error(`Failed to fetch model fragment for id ${id}`);
    }

    modelTab.insertAdjacentHTML("beforeend", await modelFragmentResponse.text());
}


/**
 * Iterates over all the texture cards in the object library and applies onclick listener to add the
 * texture to the selected model.
 */
export function applyTextureCardEventListeners() {
    const textureLoader = new TextureLoader();
    document.querySelectorAll('.add-model-btn').forEach((el) => {
        const element = el as HTMLElement;

        // Remove texture card
        if (element.dataset.type == "remove-texture") {
            // Get rid of the image element and use the div to show colour instead
            const imageContainer = element.querySelector('#image-container') as HTMLDivElement;
            const imageElement = element.querySelector('img') as HTMLImageElement;
            if (imageElement) {
                imageElement.classList.add('d-none');
            }
            if (imageContainer) {
                imageContainer.style.backgroundColor = String(0xffffff);
            }

            const triggerLoad = () => {
                const texture: PBRTextures = {
                    aoMap: null,
                    displacementMap: null,
                    map: null,
                    normalMap: null,
                    roughnessMap: null
                };
                const currentObject = SceneObjectSelector.getInstance().getSelectedObject();
                if (currentObject) {
                    setObjectTexture(texture, currentObject);
                }
            };
            element.addEventListener('click', triggerLoad);
            element.addEventListener('keydown', (event) => {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault(); // prevent scrolling for spacebar
                    triggerLoad();
                }
            });
        }

        // Normal texture cards
        if (element.dataset.type == "texture") {
            const triggerLoad = () => {
                const path = element.dataset.filepath;
                if (path) {

                    const texture: PBRTextures = {
                        aoMap: null,
                        displacementMap: null,
                        map: null,
                        normalMap: null,
                        roughnessMap: null
                    };
                    const colorTexture = textureLoader.load(path, undefined, undefined, () => {
                    });

                    if (path.endsWith("Color.jpg")) {
                        const path_ = path.slice(0, -("Color.jpg").length);
                        textureLoader.load(path_ + "AmbientOcclusion.jpg", (text) => {
                            texture.aoMap = text;
                        }, undefined, () => {
                        })
                        textureLoader.load(path_ + "Displacement.jpg", (text) => {
                            texture.displacementMap = text
                        }, undefined, () => {
                        })
                        textureLoader.load(path_ + "NormalGL.jpg", (text) => {
                            texture.normalMap = text
                        }, undefined, () => {
                        })
                        textureLoader.load(path_ + "Roughness.jpg", (text) => {
                            texture.roughnessMap = text
                        }, undefined, () => {
                        })

                    }
                    // Add a 0.1 second delay so that the textures have time to load all the part. Can be discussed at the daily scrum
                    setTimeout(() => {
                        colorTexture.colorSpace = SRGBColorSpace;
                        texture.map = colorTexture;
                        const currentObject = SceneObjectSelector.getInstance().getSelectedObject();
                        if (currentObject) {
                            setObjectTexture(texture, currentObject);
                        }
                    }, 500);
                }
            };

            element.addEventListener('click', triggerLoad);

            element.addEventListener('keydown', (event) => {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault(); // prevent scrolling for spacebar
                    triggerLoad();
                }
            });
        }
    });
}

/**
 * Change the colour of the 'No Texture' texture card.
 *
 * @param colour
 */
export function updateTextureCardBackground(colour: string) {
    const removeTextureCard = document.querySelector('.add-model-btn[data-type="remove-texture"]');
    const imageContainer = removeTextureCard?.querySelector('#image-container') as HTMLDivElement;
    if (imageContainer) {
        const newColour = colour || "#FFFFFF";
        imageContainer.style.backgroundColor = String(newColour);
    }
}

/**
 * Sets up the listeners and control logic for the sidebar, controls both activating the correct tab and closing the content section of the side menu
 */
export function setupSidebar() {
    const tabButtons: NodeListOf<HTMLButtonElement> = document.querySelectorAll('[data-tab]');
    const tabContents: NodeListOf<HTMLElement> = document.querySelectorAll('.object-library-tab-pane');
    const closeBtn = document.getElementById('closeContentBtn')!;
    const overlay = document.getElementById('overlay-controls')!;

    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const targetId = button.getAttribute('data-tab');
            if (!targetId) return;

            overlay.classList.remove('collapsed');

            tabButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');

            tabContents.forEach(tab => {
                tab.classList.remove('active');
                if (tab.id === targetId) {
                    tab.classList.add('active');
                }
            });
        });
    });

    closeBtn.addEventListener('click', () => {
        overlay.classList.add('collapsed');
        tabButtons.forEach(btn => btn.classList.remove('active'));
        tabContents.forEach(tab => tab.classList.remove('active'));
    });
}


/**
 * Function for Save button on design editor.
 * Perform front end validation and if successful, save the design using
 * saveDesign() defined in edit-design-page-controls.ts.
 */
export function createOnSubmitDesign(
        saveHandler: GLTFExportHandler,
        renderer: WebGLRenderer,
        scene: EditorScene,
): () => Promise<void> {
    return async function onSubmitDesign(): Promise<void> {
        initialiseSaveProgress();
        const isNameValid = validateDesignName();
        if (!isNameValid) {
            // Scroll up so the user can see the error message
            window.scrollTo(0, 0);
        }
        const isDescriptionValid = validateDesignDescription();
        if (isNameValid && isDescriptionValid) {
            const gltfBinary = await saveHandler.export();
            if (gltfBinary instanceof ArrayBuffer) await saveDesign(gltfBinary, renderer, scene);
        }
    }
}

export async function deleteDesign() {
    const renovationId = DesignDataProvider.getRenovationID();
    const designId = DesignDataProvider.getDesignID();
    const csrfToken = CSRFProvider.getCsrfToken();
    const csrfHeader = CSRFProvider.getCsrfHeaderName();
    const isEntry = !DesignDataProvider.isRenovationDesign();

    let url;
    if (isEntry) {
        url = `competitionEntry/${designId}/delete`;
    } else {
        url = `renovationRecord/${renovationId}/deleteDesign/${designId}`;
    }

    window.removeEventListener("beforeunload", beforeUnloadHandler);

    fetch(url, {
        method: 'POST',
        redirect: 'follow',
        headers: {
            [csrfHeader]: csrfToken,
        }
    })
    .then((res) => {
        if (res.ok) {
            if (res.url && res.url !== window.location.href) {
                window.location.href = res.url;
            }
        } else {
            throw new Error(`Request failed: ${res.status} ${res.statusText}`);
        }
    })
    .catch(() => {

        showFailToast("Error Deleting Design");

        if (DesignDataProvider.isOwned()) {
            window.addEventListener("beforeunload", beforeUnloadHandler);
        }
    });
}

// ============================== Delete custom model stuff ======================

/**
 * Send a DELETE request to delete a custom model with the given id.
 *
 * @param modelId
 */
export async function deleteCustomModel(modelId: number) {
    const url = `deleteModel/${modelId}`;
    const csrfToken = CSRFProvider.getCsrfToken();
    const csrfHeader = CSRFProvider.getCsrfHeaderName();

    fetch(url, {
        method: 'DELETE',
        headers: {
            [csrfHeader]: csrfToken
        }
    }).then((response) => {
        if (response.ok) {
            removeModelCard(modelId);
            showSuccessToast("Successfully deleted custom model");
            console.log("Deleted custom model successfully.");
        } else {
            showFailToast("Error deleting custom model");
            console.error("Failed to delete custom model.");
        }
    }).catch(() => {
        showFailToast("Error deleting custom model")
    });
}

function removeModelCard(modelId: number) {
    document.querySelectorAll('.add-model-btn').forEach(card => {
        if ((Number(card.getAttribute('data-id')) === modelId) && (card.getAttribute('data-type') === "model")) {
            card.parentElement?.remove();
        }
    });
}

/**
 * Send a DELETE request to delete a custom texture with the given id.
 *
 * @param textureId
 */
export async function deleteCustomTexture(textureId: number) {
    const url = `deleteTexture/${textureId}`;
    const csrfToken = CSRFProvider.getCsrfToken();
    const csrfHeader = CSRFProvider.getCsrfHeaderName();

    fetch(url, {
        method: 'DELETE',
        headers: {
            [csrfHeader]: csrfToken
        }
    }).then((response) => {
        if (response.ok) {
            removeTextureCard(textureId);
            showSuccessToast("Successfully deleted custom texture");
            console.log("Deleted custom texture successfully.");
        } else {
            showFailToast("Error deleting custom texture");
            console.error("Failed to delete custom texture.");
        }
    }).catch(() => {
        showFailToast("Error deleting custom texture")
    });
}

function removeTextureCard(textureId: number) {
    document.querySelectorAll('.add-model-btn').forEach(card => {
        if ((Number(card.getAttribute('data-id')) === textureId) && (card.getAttribute('data-type') === "texture")) {
            card.parentElement?.remove();
        }
    });
}

// ============================== Grapheme character counter ======================

const segmenter = new Intl.Segmenter('en', {granularity: 'grapheme'});

/**
 * Use this function to get the chracter count, where emojis are counted as
 * 1 character rather than multiple.
 * @param str text to be counted
 * @returns {number} number of characters
 */
export function countGraphemeClusters(str: string): number {
    return [...segmenter.segment(str)].length;
}

// ============================== Character counter ===============================

const descriptionElement = document.getElementById("designDescription") as HTMLTextAreaElement;
const charCounterElement = document.getElementById("charCount") as HTMLSpanElement;

/**
 * Function to be passed to the event listener, updates the character count.
 */
function updateCharacterCounter(): void {
    if (descriptionElement) {
        let charCount = countGraphemeClusters(descriptionElement.value);
        charCounterElement.innerText = charCount + "/512";
    }
}

document.addEventListener("keyup", updateCharacterCounter);
document.addEventListener("DOMContentLoaded", updateCharacterCounter);

// ============================== Save/Load progress bar ===============================
export function initialiseSaveProgress() {
    let percentComplete = 1;
    const progressBar = document.getElementById('saveProgressBar') as HTMLDivElement;
    const progressContainer = document.getElementById('progress-container') as HTMLDivElement;

    progressBar.style.width = `${percentComplete}%`;
    progressBar.setAttribute('aria-valuenow', percentComplete.toString());
    progressContainer.classList.remove('d-none');
    progressBar.textContent = `Saving... ${percentComplete.toFixed(0)}%`;

}

export function updateSaveProgress(progress: UploadProgress) {
    let percentComplete = progress.percentComplete;
    const progressBar = document.getElementById('saveProgressBar') as HTMLDivElement;
    const progressContainer = document.getElementById('progress-container') as HTMLDivElement;

    progressBar.style.width = `${percentComplete}%`;
    progressBar.setAttribute('aria-valuenow', percentComplete.toString());
    progressContainer.classList.remove('d-none');

    if (percentComplete >= 100) {
        progressBar.textContent = 'Saved!';
        progressBar.classList.remove('progress-bar-animated');
        progressBar.classList.remove('bg-success');
        progressBar.classList.add('bg-primary');
        setTimeout(closeSaveProgressBar, 2000);
    } else {
        progressBar.textContent = `Saving... ${percentComplete.toFixed(0)}%`;
    }
}

function closeSaveProgressBar() {
    const progressBar = document.getElementById('saveProgressBar') as HTMLDivElement;
    const progressContainer = document.getElementById('progress-container') as HTMLDivElement;

    progressBar.style.width = `${0}%`;
    progressBar.setAttribute('aria-valuenow', '0');
    progressBar.textContent = '';
    progressBar.classList.add('progress-bar-animated');
    progressBar.classList.add('bg-success');
    progressContainer.classList.add('d-none');
    showSuccessToast("Your design was saved successfully.");
}


export function updateLoadingProgress(progress: UploadProgress) {
    let percentComplete = progress.percentComplete;
    const progressBar = document.getElementById('loadProgressBar') as HTMLDivElement;
    const progressContainer = document.getElementById('load-progress-container') as HTMLDivElement;

    progressBar.style.width = `${percentComplete}%`;
    progressBar.setAttribute('aria-valuenow', percentComplete.toString());
    progressContainer.classList.remove('d-none');

    if (percentComplete >= 100) {
        // Pause bar at 98% to allow for other processes to happen
        progressBar.style.width = `${98}%`;
        progressBar.setAttribute('aria-valuenow', "98");
    }
}

export function closeLoadingBar() {
    const progressBar = document.getElementById('loadProgressBar') as HTMLDivElement;

    progressBar.classList.remove('progress-bar-animated');
    progressBar.classList.remove('bg-success');
    progressBar.classList.add('bg-primary');
    progressBar.style.width = `${100}%`;
    progressBar.setAttribute('aria-valuenow', '100');

    setTimeout(finishCloseLoadingBar, 600);
}

function finishCloseLoadingBar() {
    const progressBar = document.getElementById('loadProgressBar') as HTMLDivElement;
    const progressContainer = document.getElementById('load-progress-container') as HTMLDivElement;

    progressBar.style.width = `${0}%`;
    progressBar.setAttribute('aria-valuenow', '0');
    progressBar.classList.add('progress-bar-animated');
    progressBar.classList.add('bg-success');
    progressContainer.classList.add('d-none');
}