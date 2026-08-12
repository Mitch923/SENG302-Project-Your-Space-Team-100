import {EditorScene} from "@/editor-scene";
import {Object3D} from "three";

export class ModelCounter {

    private objectCount: number;
    private objectCounterElement: HTMLElement;

    /**
     * The constructor of ModelCounter takes a scene and counts up
     * all the objects already present inside of it and initialises
     * the state of the counter, and then reflects this in the UI.
     * @param scene - the scene the object counter keeps track of.
     * @param objectCounterElement - the html element that will be updated with the count of objects in the scene
     */
    constructor(scene: EditorScene, objectCounterElement: HTMLElement) {
        this.objectCount = 0;
        this.objectCounterElement = objectCounterElement;

        scene.children.forEach((object: Object3D) => {
            if (object.userData.movable) {
                this.objectCount++;
            }
        });

        this.updateCount();
    }

    /**
     * Increments the object count
     */
    public incrementObjectCount() {
        this.objectCount++;
        this.updateCount();
    }

    /**
     * Decrements the object count
     */
    public decrementObjectCount() {
        this.objectCount--;
        this.updateCount();
    }

    /**
     * Gets the object count
     */
    public getObjectCount(): number {
        return this.objectCount;
    }

    private updateCount() {
        this.objectCounterElement.innerText = `${this.objectCount}/50`;
    }
}