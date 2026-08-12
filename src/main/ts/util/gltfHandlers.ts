import {DirectionalLight, Object3D, Scene} from "three";
import {GLTFExporter, GLTFExporterOptions} from "three/examples/jsm/exporters/GLTFExporter";
import {GLTF, GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader";
import {EditorScene} from "@/editor-scene";

/**
 * Handles exporting a THREE.js scene to the GLTF format.
 *
 * This class wraps the GLTFExporter class to help with converting a given scene into either a
 * binary (.glb) or GLTF (.gltf) format.
 * Automatically disables grids during export to avoid including them in the output.
 */
class GLTFExportHandler {
    private readonly scene: EditorScene;
    private readonly options: GLTFExporterOptions;
    private exporter: GLTFExporter | null = null;

    /**
     * @param scene - The EditorScene instance to export. This is the scene that will be serialized to GLTF.
     * @param options - Optional GLTF export settings.
     */
    constructor(scene: EditorScene,
                options: GLTFExporterOptions = {
                    binary: true,
                    embedImages: false,
                    includeCustomExtensions: true
                }) {
        this.scene = scene;
        this.options = options;
    }

    /**
     * Exports the scene provided in the constructor.
     */
    public export(): Promise<ArrayBuffer | { [key: string]: unknown; }> {
        return new Promise(((resolve, reject) => {
            // Disable grid & gizmo so they are not saved
            this.scene.disableGrid();
            this.scene.remove(this.scene.transformControls.getHelper())
            this.scene.disableLights();
            this.getExporter().parse(
                    this.scene,
                    (result) => { // Successful save
                        this.scene.enableGrid();
                        this.scene.add(this.scene.transformControls.getHelper());
                        this.scene.enableLight();
                        resolve(result);
                    },
                    (err) => { // Unsuccessful save
                        this.scene.enableGrid();
                        this.scene.add(this.scene.transformControls.getHelper());
                        this.scene.enableLight();
                        reject(err);
                    },
                    this.options);
        }));

    }

    /**
     * Exports a single Object3D as a glb file using the three GLTFExporter
     * @param model Object3D to be exported
     * @param filename name of the object that will become the file name
     */
    public exportModel(model: Object3D, filename: string = "model.glb"): Promise<File> {
        return new Promise(((resolve, reject) => {
            this.getExporter().parse(
                    model,
                    (result) => {
                        try {
                            const file = new File(
                                    [result as ArrayBuffer],
                                    filename,
                                    {
                                        type: "model/gltf-binary",
                                        lastModified: Date.now()
                                    }
                            );
                            resolve(file);
                        } catch (error) {
                            reject(error);
                        }
                    },
                    (err) => {
                        reject(err);
                    },
                    this.options
            );
        }));
    }

    /**
     * Allows lazy loading of the GLTFExporter, only creating it when needed.
     * @private
     */
    private getExporter(): GLTFExporter {
        if (!this.exporter) {
            this.exporter = new GLTFExporter();
        }
        return this.exporter;
    }
}

/**
 * Class for handling the importing of a GLTF file into a scene.
 *
 */
class GLTFImportHandler {
    private readonly scene: Scene;
    private loader: GLTFLoader | null = null;

    constructor(scene: Scene) {
        this.scene = scene;
    }

    /**
     * Imports a gltf blob into the scene
     *
     * @param blob
     */
    public import(blob: Blob): Promise<void> {
        const url = URL.createObjectURL(blob);
        return new Promise((resolve, reject) => {
            this.getLoader().load(
                    url,
                    (gltf) => {
                        // Add to scene
                        gltf.scene.children.forEach((child) => {
                            if (!(child instanceof DirectionalLight)) {
                                child.parent = this.scene;
                                this.scene.children.push(child);
                                child.receiveShadow = true;

                                if (!(child.userData.displayName == 'Wall') && !(child.userData.displayName == 'Floor')) {
                                    child.traverse(((doubleChild) => {
                                        doubleChild.castShadow = true;
                                    }))
                                }
                            }
                        })

                        // Callback
                        this.onImportCallback(gltf);

                        URL.revokeObjectURL(url);
                        resolve();
                    },
                    () => {
                    },
                    (err) => {
                        console.warn(`Error attempting to load scene: `, err);
                        reject();
                    }
            );
        })
    }

    public setOnLoad(func: (gltf: GLTF) => void) {
        this.onImportCallback = func;
    }

    /**
     * Allows lazy loading of the GLTFLoader
     * @private
     */
    private getLoader(): GLTFLoader {
        if (!this.loader) {
            this.loader = new GLTFLoader();
        }
        return this.loader;
    }

    private onImportCallback: (gltf: GLTF) => void = () => {
    };
}

export {GLTFExportHandler, GLTFImportHandler};