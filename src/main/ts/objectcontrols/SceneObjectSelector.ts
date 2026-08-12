import * as THREE from 'three';
import {Vector2} from 'three';
import {OutlinePass} from "three/examples/jsm/postprocessing/OutlinePass";
import {EffectComposer} from "three/examples/jsm/postprocessing/EffectComposer";
import {ShaderPass} from "three/examples/jsm/postprocessing/ShaderPass";
import {GammaCorrectionShader} from "three/examples/jsm/shaders/GammaCorrectionShader";
import {updateCoordinates} from "@/util/objectDetailsMenu";
import {EditorScene} from "@/editor-scene";

export class SceneObjectSelector {

    private static instance: SceneObjectSelector = new SceneObjectSelector();

    private static scene: EditorScene | undefined;
    private static renderer: THREE.WebGLRenderer | undefined;
    private static camera: THREE.PerspectiveCamera | undefined;
    private static raycaster: THREE.Raycaster | undefined;
    private static outlinePass: OutlinePass | undefined;
    private mousePos: Vector2 = new THREE.Vector2();
    private startPos: Vector2 = new THREE.Vector2();
    private rayLine: THREE.Line | null = null;
    private selectedObject: THREE.Object3D | null = null;
    private isDragging: boolean = false;
    private dragThreshold: number = 5;
    private onSelect?: (object: THREE.Object3D) => void;
    private onDeselect?: (object: THREE.Object3D) => void;

    /**
     * Using singleton pattern on this class.
     * @private
     */
    private constructor() {
    }

    /**
     * Get the SceneObjectSelector instance.
     */
    static getInstance(): SceneObjectSelector {
        return this.instance
    }

    /**
     * Initialiser for the object selection controls,
     * sets up the outline rendering and the initial scene rendering adding them to the composer,
     * binds the mouse event methods to refer to the SceneObjectSelector class instance when **this** is used.
     *
     * @param scene The 3d scene the objects are in
     * @param renderer the renderer for capturing the mouse events
     * @param camera Main camera in the scene to use for getting the origin for the ray-tracing
     * @param composer Composer to layer the highlighting effects on top of the scene
     */
    static init(scene: EditorScene, renderer: THREE.WebGLRenderer, camera: THREE.PerspectiveCamera, composer: EffectComposer) {
        const sceneObjectSelector: SceneObjectSelector = SceneObjectSelector.getInstance();
        this.scene = scene;
        this.camera = camera;
        this.renderer = renderer;

        this.raycaster = new THREE.Raycaster();
        this.raycaster.layers.set(0); // Only interact with object layer

        this.outlinePass = new OutlinePass(renderer.getSize(new Vector2()), this.scene, this.camera);
        this.outlinePass.renderToScreen = true;
        composer.addPass(this.outlinePass);

        this.outlinePass.edgeStrength = 1;
        this.outlinePass.edgeGlow = 1;
        this.outlinePass.visibleEdgeColor.set(0xffffff);
        this.outlinePass.hiddenEdgeColor.set(0xffffff);

        const copyPass = new ShaderPass(GammaCorrectionShader); // corrects the colours so they don't look dark
        composer.addPass(copyPass);
        sceneObjectSelector.onMouseDown = sceneObjectSelector.onMouseDown.bind(sceneObjectSelector);
        sceneObjectSelector.onMouseMove = sceneObjectSelector.onMouseMove.bind(sceneObjectSelector);
        sceneObjectSelector.onMouseUp = sceneObjectSelector.onMouseUp.bind(sceneObjectSelector);

        // Setup event handlers
        this.renderer.domElement.addEventListener("mousedown", sceneObjectSelector.onMouseDown);
        // Handled by event manager now
    }

    /**
     * Mouse down listener<br>
     * Applies Mouse movement listeners on mouse down.
     *
     * @param e the mouse event information
     * @public
     */
    public onMouseDown(e: MouseEvent) {

        // Starting position of the drag
        this.startPos.x = e.clientX;
        this.startPos.y = e.clientY;

        this.isDragging = false; // Reset
        this.updateMousePos(e);

        // Remove event listeners
        if (SceneObjectSelector.renderer) {
            SceneObjectSelector.renderer.domElement.addEventListener("mousemove", this.onMouseMove);
            SceneObjectSelector.renderer.domElement.addEventListener("mouseup", this.onMouseUp);
        }
    }

    public setOnSelect(callback: (object: THREE.Object3D) => void) {
        this.onSelect = callback;
    }

    public setOnDeselect(callback: (object: THREE.Object3D) => void) {
        this.onDeselect = callback;
    }

    /**
     * Gets currently selected object
     * @return selectedObject
     */
    public getSelectedObject() {
        return this.selectedObject;
    }

    public getScene() {
        return SceneObjectSelector.scene;
    }

    /**
     * Deselects an object by removing the white outline
     * @private
     */
    deselect() {
        if (this.selectedObject) {
            this.onDeselect?.(this.selectedObject);
            this.selectedObject = null;
            if (SceneObjectSelector.outlinePass) {
                SceneObjectSelector.outlinePass.selectedObjects = []; // Clear on deselect
            }
        }
    }

    /**
     * Updates the mouse position components.
     *
     * @param e the mouse event
     * @private
     */
    private updateMousePos(e: MouseEvent) {
        if (SceneObjectSelector.renderer) {
            const canvas = SceneObjectSelector.renderer.domElement;
            const rect = canvas.getBoundingClientRect();

            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            this.mousePos.x = (x / rect.width) * 2 - 1;
            this.mousePos.y = -(y / rect.height) * 2 + 1;
        }
    }

    /**
     * Mouse move listener<br>
     * Checks if the distance the mouse was moved
     * after mouse down is larger than some threshold and flips {@Code isDragging = true} if so
     *
     * @param e mouse event
     * @private
     */
    private onMouseMove(e: MouseEvent) {
        // Change in distance
        const delta = new THREE.Vector2();
        delta.x = Math.abs(e.clientX - this.startPos.x);
        delta.y = Math.abs(e.clientY - this.startPos.y);
        if (delta.x > this.dragThreshold || delta.y > this.dragThreshold) {
            this.isDragging = true;
        }

        if (this.selectedObject !== null) {
            updateCoordinates(this.selectedObject);
        }
    }

    /**
     * Mouse up listener<br>
     * Does nothing but remove listeners if user is dragging camera.<br>
     * If user is not dragging, casts ray into scene to perform selection<br>
     *
     * @param e mouse event
     * @private
     */
    private onMouseUp(e: MouseEvent) {
        if (!this.isDragging && SceneObjectSelector.raycaster && SceneObjectSelector.camera && SceneObjectSelector.scene) {
            // Select object
            this.updateMousePos(e);
            SceneObjectSelector.raycaster.setFromCamera(this.mousePos, SceneObjectSelector.camera);
            const objects = SceneObjectSelector.scene.children.filter(obj => obj.userData.unselectable !== true);
            const intersects = SceneObjectSelector.raycaster.intersectObjects(objects, true);

            if (this.rayLine) {
                SceneObjectSelector.scene.remove(this.rayLine);
                this.rayLine.geometry.dispose();
                this.rayLine = null;
            }

            if (intersects.length > 0) {
                if (SceneObjectSelector.scene.getObjectByName(intersects[0].object.userData.parentGroup)) {
                    this.select(
                            SceneObjectSelector.scene.getObjectByName(intersects[0].object.userData.parentGroup) as THREE.Object3D)
                } else {
                    this.select(intersects[0].object);
                }
            } else {
                // Hit nothing so deselect previous object
                this.deselect();
            }
        }
    }

    /**
     * Gets all the meshes of an object
     * @param object - object to get meshes from
     * @private
     */
    private getMeshes(object: THREE.Object3D): THREE.Mesh[] {
        const meshes: THREE.Mesh[] = [];
        object.traverse((child) => {
            if ((child as THREE.Mesh).isMesh) {
                meshes.push(child as THREE.Mesh);
            }
        });
        return meshes;
    }

    /**
     * Applies the white outline to the object that has been selected and deselects a previously selected object
     * @param object The first object that the raytrace intersected with that should have the white outline applied
     * @private
     */
    private select(object: THREE.Object3D) {
        while (object.parent != SceneObjectSelector.scene && object.parent != null) {
            object = object.parent;
        }
        if (this.selectedObject !== object && SceneObjectSelector.outlinePass) {
            this.deselect(); // Deselect previous object
            this.selectedObject = object;
            // find meshes and set outline, should hopefully optimise what the outline pass has to process
            SceneObjectSelector.outlinePass.selectedObjects = this.getMeshes(object);
            this.onSelect?.(this.selectedObject);
        }
    }
}