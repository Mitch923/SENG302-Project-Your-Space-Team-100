import {
    AmbientLight,
    CameraHelper,
    DirectionalLight,
    GridHelper,
    PerspectiveCamera,
    Scene,
    WebGLRenderer
} from 'three';
import {TransformControls} from "three/examples/jsm/controls/TransformControls";
import {EditorCamera} from "./editor-camera";
import {OrbitControls} from "three/examples/jsm/controls/OrbitControls";

/**
 * Extension of three js scene for custom functionality
 *
 */
export class EditorScene extends Scene {

    private static readonly SHADOWS_WIDTH: number = 40; // Increase this value to make the shadows cover a larger area
    private static readonly SHADOWS_QUALITY: number = 2048;
    public transformControls: TransformControls;
    public camera: PerspectiveCamera;
    public orbitControls: OrbitControls;
    private grid: GridHelper;
    private readonly directionalLight: DirectionalLight;
    private readonly ambientLight: AmbientLight;

    /**
     * @param renderer - The renderer for the editor scene
     */
    constructor(renderer: WebGLRenderer) {
        super();

        this.camera = new EditorCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
        this.camera.position.set(10, 8, 10);
        this.camera.lookAt(0, 2, 0);

        this.transformControls = new TransformControls(this.camera, renderer.domElement);
        this.transformControls.getHelper().userData.unselectable = true;
        this.add(this.transformControls.getHelper())

        // Configure default grid
        this.grid = new GridHelper(30, 30, 0x888888, 0x888888);
        this.grid.layers.set(2); // Layer 0 is scene, 1 is tools, 2 here for grid
        this.grid.position.y = -0.01;
        this.add(this.grid);

        this.orbitControls = new OrbitControls(this.camera, renderer.domElement);
        this.orbitControls.enableDamping = true;

        // Add a default light
        this.directionalLight = new DirectionalLight(0xfff8e7, 2);
        this.directionalLight.position.set(10, 10, 10);
        this.directionalLight.castShadow = true;
        this.directionalLight.shadow.bias = -0.001;
        this.directionalLight.shadow.normalBias = 0.02;
        this.calibrateDirectionalLight();

        this.add(this.directionalLight);

        this.ambientLight = new AmbientLight(0xFFFFFF, 0.5);
        this.add(this.ambientLight);
    }

    public clear(): this {
        super.clear();
        this.add(this.grid); // Ensure grid stays
        return this;
    }

    public setGrid(grid: GridHelper) {
        this.remove(this.grid);
        this.grid = grid;
        this.add(this.grid);
    }

    public disableGrid(): void {
        this.remove(this.grid);
    }

    public enableGrid(): void {
        this.add(this.grid);
    }

    public disableLights(): void {
        this.remove(this.directionalLight);
        this.remove(this.ambientLight);
    }

    public enableLight(): void {
        this.add(this.directionalLight);
        this.add(this.ambientLight);
    }

    /**
     * For debugging purposes, adds a visualisation of the camera position and reach
     */
    public addDirectionalLightCameraHelper(): void {
        const helper = new CameraHelper(this.directionalLight.shadow.camera);
        this.add(helper);
    }

    /**
     * Calibrates the directional light for the scene. Increases the default shadow resolution from
     * 512x512 to 2048x2048. Changes the reach of the shadow camera according to the value specified
     * in EditorScene.SHADOWS_WIDTH
     */
    public calibrateDirectionalLight(): void {
        this.directionalLight.shadow.mapSize.width = EditorScene.SHADOWS_QUALITY;
        this.directionalLight.shadow.mapSize.height = EditorScene.SHADOWS_QUALITY;

        this.directionalLight.shadow.camera.left = EditorScene.SHADOWS_WIDTH / -2;
        this.directionalLight.shadow.camera.right = EditorScene.SHADOWS_WIDTH / 2;
        this.directionalLight.shadow.camera.top = EditorScene.SHADOWS_WIDTH / 2;
        this.directionalLight.shadow.camera.bottom = EditorScene.SHADOWS_WIDTH / -2;
        this.directionalLight.shadow.camera.near = 0.5;
        this.directionalLight.shadow.camera.far = 50; // Increase this value to make the shadows appear further from the source light
    }
}