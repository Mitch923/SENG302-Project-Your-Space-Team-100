import * as THREE from 'three';
import {Mesh, MeshStandardMaterial, Object3D, SRGBColorSpace, Vector3, WebGLRenderer} from 'three';
import {EditorScene} from "./editor-scene";
import {ModelLoader} from "./ModelLoader";
import {GLTFExportHandler} from "./util/gltfHandlers";
import {saveScreenshot, ScreenShotParams, takeScreenshot} from "./util/renderer-screenshot";
import {clone as cloneObject} from 'three/examples/jsm/utils/SkeletonUtils';

const filePicker = document.getElementById('model-upload-input') as HTMLInputElement;
const nameInput = document.getElementById("modelName") as HTMLInputElement;

let renderer: WebGLRenderer | null = null;
let scene: EditorScene | null = null;

let modelLoader: ModelLoader;

let exportHandler: GLTFExportHandler;


// Init on load
export async function initPreview(file: File) {
    renderer = new THREE.WebGLRenderer({antialias: true});
    scene = new EditorScene(renderer);
    modelLoader = new ModelLoader(scene);

    document.getElementById("previewModal")?.addEventListener('shown.bs.modal', () => {
        window.addEventListener('resize', () => resizeRendererToContainer(renderer, scene));
        resizeRendererToContainer(renderer, scene);
    });

    renderer.outputColorSpace = SRGBColorSpace;
    scene.background = new THREE.Color(0xe3e3e3);
    scene.disableGrid();
    document.getElementById('previewSceneContainer')?.appendChild(renderer.domElement);

    // === Lights ===
    const ambientLight = new THREE.AmbientLight(0xffffff, 1.5);
    scene.add(ambientLight);
    scene.camera.position.set(0, 4, 3);
    scene.orbitControls.update();

    await modelLoader.loadModelFromFile(file, file.name);

    renderer.setAnimationLoop(() => animate(renderer, scene));


}

// when the modal closes tear down the scene
const previewModal = document.getElementById('previewModal');
const fileInput = document.getElementById('model-upload-input') as HTMLInputElement;
previewModal?.addEventListener('hidden.bs.modal', () => {
    scene?.clear()
    scene?.disableGrid()
    if (renderer) {
        renderer.setAnimationLoop(null);
    }
    if (nameInput) {
        nameInput.value = "";
    }
    fileInput.value = ""; // clear the file input to prevent the "change" event not getting triggered next time
    document.getElementById('modelName')?.classList.remove('is-invalid');
    renderer?.domElement.remove();
    renderer?.dispose();
});

/**
 * Called when the upload button is pressed and takes the model loaded into the scene converts it to a GLB then returns this.
 */
export async function uploadModel(): Promise<File | undefined> {
    if (scene) {
        exportHandler = new GLTFExportHandler(scene);
        const foundObjects: Object3D[] = [];

        scene.traverse((child) => {
            if (child.userData.movable === true) {
                foundObjects.push(child);
            }
        });

        const toExport = cloneObject(foundObjects[0]);
        toExport.traverse((child) => {
            if (child instanceof Mesh) {
                const originalMaterials = Array.isArray(child.material) ? child.material : [child.material];

                const clonedMaterials = originalMaterials.map((mat: MeshStandardMaterial) => {
                    const clonedMat = mat.clone();

                    // Remove the parallel texture for export
                    if (clonedMat.userData?.isParallelTexture) {
                        clonedMat.map = null;  // or set to baked map if you have one
                        clonedMat.needsUpdate = true;

                        delete clonedMat.userData.isParallelTexture;
                        delete clonedMat.userData.originalMap;
                    }

                    return clonedMat;
                });

                child.material = Array.isArray(child.material) ? clonedMaterials : clonedMaterials[0];
            }
        });
        return await exportHandler.exportModel(toExport);
    }
}


/**
 * Called continuously to update the various properties of the scene such as controls and rendering the scene at every frame
 */
function animate(renderer: WebGLRenderer | null, scene: EditorScene | null) {
    if (renderer && scene !== null) {
        scene.orbitControls.update();
        renderer.render(scene, scene.camera);
    }
}

/**
 * Called when the scene first loads and on screen resize to manage the size of the container on different screen sizes.
 * @param renderer render to set the size and pixel ration of the scene to render
 * @param scene scene to access the camera to configure the aspect ratio
 */
function resizeRendererToContainer(renderer: WebGLRenderer | null, scene: EditorScene | null) {
    const container = document.getElementById('previewSceneContainer');
    if (!container) return;

    const width = container.offsetWidth;
    const height = container.offsetHeight;

    if (renderer && scene !== null) {
        renderer.setSize(width, height);
        renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

        scene.camera.aspect = width / height;
        scene.camera.updateProjectionMatrix();
    }

}

/**
 * Take a screenshot for the new custom model's thumbnail image, and save it.
 *
 * @param modelId {number} new custom models id.
 */
export async function takeModelScreenshot(modelId: number) {
    if (renderer && scene) {
        const screenShotParams: ScreenShotParams = {
            renderer: renderer,
            scene: scene,
            imageType: 'jpeg',
            quality: 0.8,
            width: 256,
            height: 512,
            cameraPosition: new Vector3(0, 6, 10),
            cameraTarget: new Vector3(0, 1, 0),
            resetScene: false
        };
        const screenshot = await takeScreenshot(screenShotParams);
        if (screenshot) {
            await saveScreenshot(screenshot, `upload/image/model/${modelId}`);
        }
    }
}

/**
 * Returns the instance of model loader used to load the preview scene
 * @return modelLoader
 */
export function getPreviewModelLoader() {
    return modelLoader;
}