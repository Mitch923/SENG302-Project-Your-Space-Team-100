import {
    Box3,
    Group,
    Light,
    MeshStandardMaterial,
    Object3D,
    Scene,
    Texture,
    TextureLoader,
    Vector3
} from "three";
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader";
import {OBJLoader} from "three/examples/jsm/loaders/OBJLoader";
import {FBXLoader} from "three/examples/jsm/loaders/FBXLoader";
import {changeObjectWarmth} from "@/objectcontrols/LightingControls";
import {PBRTextures} from "@/edit-design-page-controls";
import {setObjectTexture} from "@/objectcontrols/objectManipulator";

/**
 * Used to load an individual model into the scene
 */
export class ModelLoader {
    private readonly scene: Scene;
    private mostRecentlyLoadedModel: Object3D | null = null;
    private mostRecentlyLoadedOriginalTextureMap: Map<MeshStandardMaterial, Texture | null> = new Map();
    private mostRecentlyLoadedParallelTexture: File | null = null;

    constructor(scene: Scene) {
        this.scene = scene;
    }

    /**
     * Used to load an individual model, if it is an obj or a gltf
     * @param filePath the path to the model file
     * @param modelName the name of the model to be created
     * @param textureFilePath optional path of the texture to apply on load
     */
    public async loadModel(filePath: string, modelName: string, textureFilePath?: string): Promise<void> {
        if (filePath.toLowerCase().endsWith(".gltf") || filePath.toLowerCase().endsWith(".glb")) {
            await this.loadGLTF(filePath, modelName);
        } else if (filePath.toLowerCase().endsWith(".obj")) {
            await this.loadOBJ(filePath, modelName);
        } else {
            throw new Error(`Unsupported file format: ${filePath}`);
        }

        if (textureFilePath) {
            this.applyTextureToMostRecentlyLoadedModel(textureFilePath);
        }
    }

    /**
     * Load a model from a file determines the specific loader based on the file extension
     * @param file The model of some type to be loaded
     * @param fileName Name of the file that should include the extension
     */
    public loadModelFromFile(file: File, fileName: string): Promise<void> {
        return new Promise((resolve, reject) => {
            const fileNameLower = fileName.toLowerCase();
            const fileLoader = new FileReader();
            fileLoader.readAsArrayBuffer(file);
            fileLoader.onload = () => {
                const arrayBuffer = fileLoader.result as ArrayBuffer;
                if (arrayBuffer) {
                    if (fileNameLower.endsWith(".glb")) {
                        this.loadGLBFromBuffer(arrayBuffer, fileNameLower).then(() => resolve()).catch(reject);
                    } else if (fileNameLower.endsWith(".fbx")) {
                        this.loadFBXFromBuffer(arrayBuffer, fileNameLower).then(() => resolve()).catch(reject);
                    } else if (fileNameLower.endsWith(".obj")) {
                        this.loadOBJFromBuffer(arrayBuffer, fileNameLower).then(() => resolve()).catch(reject);
                    } else {
                        reject(new Error(`Unsupported file format: ${fileName}`));
                    }
                }
            }
        });
    }

    /**
     * Returns the model most recently loaded by the instance of ModelLoader
     * @return mostRecentlyLoadedModel
     */
    public getMostRecentlyLoadedModel() {
        return this.mostRecentlyLoadedModel;
    }

    /**
     * Returns a deep clone of the most recently loaded model by the instance of ModelLoader. This
     * will be the model in the raw uploaded format without any parallel textures
     * @return mostRecentlyLoadedOriginalTextureMap
     */
    public getMostRecentlyLoadedOriginalTextureMap() {
        return this.mostRecentlyLoadedOriginalTextureMap;
    }

    /**
     * Returns the most recently uploaded parallel texture file to be saved alongside the uploaded model
     */
    public getMostRecentlyLoadedParallelTexture() {
        return this.mostRecentlyLoadedParallelTexture;
    }

    /**
     * Sets the most recently uploaded parallel texture to the given file
     * @param file to set
     */
    public setMostRecentlyLoadedParallelTexture(file: File) {
        this.mostRecentlyLoadedParallelTexture = file;
    }

    // /**
    //  * Swaps the model in the scene to the untextured version
    //  */
    // public swapToUntexturedModel() {
    //     if (this.mostRecentlyLoadedOriginalTextureMap) {
    //         this.scene.add(this.mostRecentlyLoadedOriginalTextureMap);
    //     }
    //     if (this.mostRecentlyLoadedModel) {
    //         this.scene.remove(this.mostRecentlyLoadedModel);
    //     }
    // }
    //
    // /**
    //  * Swaps the texture in the scene to the textured version
    //  */
    // public swapToTexturedModel() {
    //     if (this.mostRecentlyLoadedModel) {
    //         this.scene.add(this.mostRecentlyLoadedModel);
    //     }
    //     if (this.mostRecentlyLoadedOriginalTextureMap) {
    //         this.scene.remove(this.mostRecentlyLoadedOriginalTextureMap);
    //     }
    // }

    /**
     * Logic for scaling, positioning, and grouping loaded models. Updates the most recently loaded model
     * @param object3D The loaded 3D object to process
     * @param modelName The display name for the model
     */
    private processLoadedModel(object3D: Object3D, modelName: string): void {
        const box = new Box3().setFromObject(object3D);
        const size = new Vector3();
        box.getSize(size);
        const targetHeight = 2.5;
        const scaleFactor = targetHeight / Math.max(size.x, size.y, size.z);

        const offset = new Vector3();
        box.getCenter(offset);
        offset.sub(new Vector3(0, size.y / 2, 0));
        offset.multiply(new Vector3(scaleFactor, scaleFactor, scaleFactor));

        object3D.scale.set(scaleFactor, scaleFactor, scaleFactor);
        object3D.position.sub(offset);

        // Objects are identified by UUID in their .name as this is the only attribute persisted across saves
        const group = new Group();
        const groupUUID = crypto.randomUUID();
        group.name = groupUUID;
        group.add(object3D);

        // Set parent group reference for all children
        group.traverse((child: Object3D) => {
            child.userData.parentGroup = groupUUID;
            child.castShadow = true;
            child.receiveShadow = true;
            if (child instanceof Light) {
                child.shadow.bias = -0.001;
                child.shadow.normalBias = 0.02;
                child.shadow.mapSize.height = 2048;
                child.shadow.mapSize.weight = 2048;
                group.userData.isLight = true;
                group.userData.lightIntensity = 50;
                group.userData.warmth = 50;
                child.intensity = 50;
            }
        });
        changeObjectWarmth(group, 50);
        group.receiveShadow = true;

        object3D.name = groupUUID;
        group.userData.movable = true;
        group.userData.displayName = modelName;
        group.userData.scale = 1;

        this.mostRecentlyLoadedModel = group;
        this.scene.add(group);
    }

    /**
     * Used to load an OBJ model
     * @param filePath the path to the obj file
     * @param modelName the name of the model to be created
     */
    private async loadOBJ(filePath: string, modelName: string): Promise<void> {
        return new Promise((resolve, reject) => {
            const loader = new OBJLoader();
            loader.load(
                    filePath,
                    (object) => {
                        this.processLoadedModel(object, modelName);
                        resolve();
                    },
                    undefined,
                    (err) => reject(err)
            );
        });
    }

    /**
     * Used to load a GLTF model
     * @param filePath path to the gltf file
     * @param modelName name of the model
     */
    private async loadGLTF(filePath: string, modelName: string): Promise<void> {
        return new Promise((resolve, reject) => {
            const loader = new GLTFLoader();
            loader.load(
                    filePath,
                    (gltf) => {
                        this.processLoadedModel(gltf.scene, modelName);
                        resolve();
                    },
                    undefined,
                    (err) => reject(err)
            );
        });
    }

    /**
     * Used to load an OBJ model when you have the model file rather than a file path
     * @param buffer The model to load into the scene
     * @param modelName display name of the model to be displayed in the object details field etc
     * @private
     */
    private loadOBJFromBuffer(buffer: ArrayBuffer, modelName: string): Promise<void> {
        return new Promise((resolve, reject) => {
            try {
                const decoder = new TextDecoder();
                const data = decoder.decode(buffer);
                const loader = new OBJLoader();
                const object = loader.parse(data);
                this.processLoadedModel(object, modelName);
                resolve();
            } catch (err) {
                reject(err);
            }
        });
    }

    /**
     * Used to load a FBX model when you have the model file rather than a file path
     * @param buffer The model to load into the scene
     * @param modelName display name of the model to be displayed in the object details field etc
     * @private
     */
    private loadFBXFromBuffer(buffer: ArrayBuffer, modelName: string): Promise<void> {
        return new Promise((resolve, reject) => {
            try {
                const loader = new FBXLoader();
                const object = loader.parse(buffer, '');
                this.processLoadedModel(object, modelName);
                resolve();
            } catch (err) {
                reject(err);
            }
        });
    }

    /**
     * Used to load a GLB model when you have the model file rather than a file path
     * @param buffer The model to load into the scene
     * @param modelName display name of the model to be displayed in the object details field etc
     * @private
     */
    private loadGLBFromBuffer(buffer: ArrayBuffer, modelName: string): Promise<void> {
        return new Promise((resolve, reject) => {
            const loader = new GLTFLoader();
            loader.parse(
                    buffer,
                    '',
                    (gltf) => {
                        this.processLoadedModel(gltf.scene, modelName);
                        resolve();
                    },
                    (err) => reject(err)
            );
        });
    }

    private async applyTextureToMostRecentlyLoadedModel(textureFilePath: string) {
        const textureLoader = new TextureLoader();
        const texture = textureLoader.load(textureFilePath, undefined, undefined, () => {
        });

        const pbrTextures: PBRTextures = {
            aoMap: null,
            map: texture,
            displacementMap: null,
            normalMap: null,
            roughnessMap: null
        }

        if (this.mostRecentlyLoadedModel != null) {
            setObjectTexture(pbrTextures, this.mostRecentlyLoadedModel);
        }
    }
}