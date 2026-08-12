import * as THREE from "three";
import {Object3D} from "three";

/**
 * Provides a method to change the colour of objects.
 */
export class ColourPicker {

    /**
     * Changes the currently selected object's colour to the provided hex value
     *
     * ChatGPT was used to fix compiler errors with ThreeJS types not wanting to cooperate with
     * the .isMesh attribute of meshes and the material .color attribute
     *
     * @param object
     * @param colour - a colour hex value to set the object's colour to
     */
    public changeObjectColour(object: Object3D, colour: String) {
        // set the selected object's colour attribute to equal the new colour
        object.userData.colour = colour;

        // traverse through child objects, find meshes, get their materials and set
        // the .color attribute to reflect the new colour
        object.traverse((child) => {
            if (this.isMesh(child)) {
                if (Array.isArray(child.material)) {
                    child.material.forEach((mat) => {
                        if (mat && 'color' in mat) {
                            (mat as THREE.Material & {
                                color: THREE.Color
                            }).color.set(colour as any);
                        }
                    });
                } else if (child.material && 'color' in child.material) {
                    (child.material as THREE.Material & {
                        color: THREE.Color
                    }).color.set(colour as any);
                }
            }
        });
    }

    /**
     * Helper function to determine if the object passed is a mesh.
     * This is to get around the TS compiler complaining about the
     * Mesh.isMesh not existing on Object3D types.
     *
     * @param obj - the 3D object to check
     * @private
     */
    private isMesh(obj: THREE.Object3D): obj is THREE.Mesh {
        return (obj as THREE.Mesh).isMesh;
    }
}