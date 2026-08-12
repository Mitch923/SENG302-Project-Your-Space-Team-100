import {Color, PCFSoftShadowMap, SRGBColorSpace, WebGLRenderer, WebGLRenderTarget} from 'three';
import {EditorScene} from "./editor-scene";
import {
    applyButtonEventListeners,
    applyModelCardEventListeners,
    applyTextureCardEventListeners,
    beforeUnloadHandler,
    closeLoadingBar,
    insertAllPublicModels,
    insertAllUploadedModels,
    setupDeleteModal,
    setupSidebar,
    updateLoadingProgress
} from "./edit-design-page-controls";
import {SceneObjectSelector} from "./objectcontrols/SceneObjectSelector";
import {EffectComposer} from "three/examples/jsm/postprocessing/EffectComposer";
import {RenderPass} from "three/examples/jsm/postprocessing/RenderPass";
import {GLTFExportHandler, GLTFImportHandler} from "./util/gltfHandlers";
import {showSuccessToast} from "./util/toastNotifications";
import {createDefaultScene} from "./default-scene-builder";
import {ObjectController} from "./objectcontrols/ObjectController";
import {ModelCounter} from "./util/ModelCounter";
import {setUpModelUploading} from "./util/resourceUploading";
import {saveScreenshot, takeScreenshot} from "@/util/renderer-screenshot";
import {DesignDataProvider} from "@/util/designDataProvider";
import {DownloadConfig, DownloadManager} from "@/chunking/downloading/DownloadManager";


// === Inject owned boolean from the DOM ===
const ownedString: string = (document.getElementById("owned") as HTMLInputElement).value;
const owned: boolean = ownedString.trim().toLowerCase() === "true";

// === Design data ===
const renovationId = parseInt((document.getElementById("renovationId") as HTMLInputElement).value, 10);
const designId = parseInt((document.getElementById("designId") as HTMLInputElement).value, 10);

// === Renderer setup ===
const width = window.innerWidth;
const height = window.innerHeight * 0.75;
const pixelRatio = Math.min(window.devicePixelRatio, 2); // Makes the max CSS pixel ratio 2 to prevent performance issues

const renderer = new WebGLRenderer({antialias: true});
renderer.shadowMap.enabled = true;
renderer.shadowMap.type = PCFSoftShadowMap;

renderer.outputColorSpace = SRGBColorSpace;
renderer.setSize(width, height);
renderer.setPixelRatio(pixelRatio);
document.getElementById("canvas-container")?.appendChild(renderer.domElement);
const scene = new EditorScene(renderer);
scene.background = new Color(0xe3e3e3);

// === Postprocessing composer ===
const renderTarget = new WebGLRenderTarget(width, height, {samples: 4}); // Declare an explicit render target so that the composer doesn't default to a low quality one
const composer = new EffectComposer(renderer, renderTarget);
composer.setSize(width, height);
composer.setPixelRatio(pixelRatio);

// === Composer ===
const renderPass = new RenderPass(scene, scene.camera);
renderPass.setSize(width, height);
composer.addPass(renderPass);

// === Handlers ===
const saveHandler = new GLTFExportHandler(scene);
const importHandler = new GLTFImportHandler(scene);

// Init on load
window.onload = () => init(owned);

// === Object Selector ===
if (owned) {
    scene.transformControls.addEventListener('dragging-changed', function (event) {
        scene.orbitControls.enabled = !event.value;
    });
}

// === Object Counter ===
let modelCounter: ModelCounter | null = null;

async function init(owned: boolean) {

    // show a creation success message if the created model attribute is present
    // NOTE: please refactor this as it is janky as, do it when we do the floorplan story
    const createdField = document.getElementById("created") as HTMLInputElement | null;
    if (createdField != null) {
        const createdValue = createdField.value;
        if (createdValue) {
            showSuccessToast("Your design was created successfully");
        }
    }

    const defaultSceneUsed = await importScene();
    applyButtonEventListeners(saveHandler, renderer, scene);
    if (owned) {
        window.addEventListener("beforeunload", beforeUnloadHandler);

        const modelCounterElement = document.getElementById("model-counter") as HTMLElement;
        modelCounter = new ModelCounter(scene, modelCounterElement);

        // === Set up any object controls and pass them to the ObjectController ===
        SceneObjectSelector.init(scene, renderer, scene.camera, composer);

        new ObjectController(modelCounter);
        if (defaultSceneUsed) {
            // Take and save screenshot if default scene was loaded
            const blob = await takeScreenshot({
                renderer,
                scene,
                imageType: 'jpeg'
            });
            if (blob) {
                let thumbnailEndpoint;
                if (DesignDataProvider.isRenovationDesign()) {
                    thumbnailEndpoint = `upload/image/design/${designId}`;
                } else {
                    thumbnailEndpoint = `upload/image/competitionEntry/${designId}`;
                }
                await saveScreenshot(blob, thumbnailEndpoint);
            } else {
                console.warn("Was unable to save screenshot for design {}", designId);
            }
        }

        closeLoadingBar();
        await insertAllPublicModels();
        await insertAllUploadedModels();
        applyTextureCardEventListeners();
        applyModelCardEventListeners(scene, modelCounter);
        setupSidebar();
        setUpModelUploading(scene, modelCounter);
        setupDeleteModal();

    } else {
        const objectCounterContainer = document.getElementById("model-counter-container") as HTMLElement;
        if (objectCounterContainer) {
            objectCounterContainer.hidden = true;
        }
        closeLoadingBar();
    }
}

/**
 * Imports the scene data related to the current design, if no design data is found, then it triggers loading of default scene data
 *
 * @return Promise<boolean> that will resolve to whether the default was used or not
 */
async function importScene(): Promise<boolean> {
    let url = `renovationRecord/${renovationId}/getDesignData/${designId}`;
    if (!DesignDataProvider.isRenovationDesign()) {
        url = `getCompetitionEntryData/${designId}`;
    }

    const config = {
        designId: designId,
        isCompetition: !DesignDataProvider.isRenovationDesign(),
        url: url
    } as DownloadConfig;

    const downloadManager = new DownloadManager();
    downloadManager.onProgress = (progress) => updateLoadingProgress(progress);

    let blob: Blob;
    try{
        blob = await downloadManager.getFile(config);
        try {
            await importHandler.import(blob);
        } catch (e) {
            console.error("Unable to load blob into scene!")
            await createDefaultScene(scene);
        }
    } catch (e) {
        console.info(`Error occurred getting scene, loading default scene`);
        await createDefaultScene(scene);
    }
    return true;
}

scene.camera.position.set(0, 10, 5);
scene.orbitControls.update();

/**
 * Called continuously to update the various properties of the scene such as controls and rendering the scene at every frame
 */
function animate() {
    scene.orbitControls.update();
    composer.render();
}

renderer.setAnimationLoop(animate);

// === Resize handler ===
function handleScreenResize() {
    const newWidth = window.innerWidth;
    const newHeight = window.innerHeight * 0.75;
    const newPixelRatio = Math.min(window.devicePixelRatio, 2);

    scene.camera.aspect = newWidth / newHeight;
    scene.camera.updateProjectionMatrix();

    renderer.setSize(newWidth, newHeight);
    renderer.setPixelRatio(newPixelRatio);

    composer.setSize(newWidth, newHeight);
    composer.setPixelRatio(newPixelRatio);
    composer.passes.forEach(pass => {
        if (pass.setSize) pass.setSize(newWidth, newHeight);
    });

    renderTarget.setSize(newWidth, newHeight);
}

window.addEventListener('resize', handleScreenResize);
handleScreenResize();