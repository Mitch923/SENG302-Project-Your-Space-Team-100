import {Box3, Color, Euler, Mesh, MeshStandardMaterial, Object3D, Vector3} from "three";
import {EditorScene} from "@/editor-scene";
import {ModelCounter} from "@/util/ModelCounter";
import {PBRTextures} from "@/edit-design-page-controls";

/**
 * Positions sets the objects position to y=0 which is the height of the top of the floor
 * @param object the object that should be snapped to the floor
 */
export function snapObjectToFloor(object: Object3D) {
    let precise = true;
    // A more precise bounding box is required when the object is rotated
    if (object.rotation.equals(new Euler(0, 0, 0))) {
        precise = false;
    }
    const box = new Box3().setFromObject(object, precise);
    const size = new Vector3();
    box.getSize(size);

    const offset = new Vector3();
    box.getCenter(offset);

    object.position.y = object.position.y - offset.y + size.y / 2;
}

export function removeObject(scene: EditorScene, obj: Object3D, modelCounter: ModelCounter) {
    if (obj && obj.userData.movable) {
        obj.parent?.remove(obj);
        scene.transformControls.detach();
        if (modelCounter.getObjectCount() >= 1) {
            modelCounter.decrementObjectCount();
        } else {
            console.error("ERROR removeObject: ObjectCount is < 1");
        }
    }
}

/**
 * Scales the object by the given scale
 *
 * @param object the object to be scaled
 * @param scale the scale to scale the object by
 */
export function scaleObject(object: Object3D, scale: number) {
    object.scale.set(scale, scale, scale);
    object.userData.scale = scale;
}

/**
 * Set the texture of the selected object to the texture selected in the panel
 * @param textures
 * @param object
 */
export function setObjectTexture(textures: PBRTextures, object: Object3D) {
    object.traverse((child) => {
        if (child instanceof Mesh) {

            if (Array.isArray(child.material)) {
                child.material.forEach((mat) => {
                    mat = new MeshStandardMaterial();
                    applyPBRTexture(mat, textures);
                    mat.needsUpdate = true;
                });
            } else if (child.material && 'map' in child.material) {
                child.material = new MeshStandardMaterial();
                applyPBRTexture(child.material, textures);
                child.material.needsUpdate = true;
            }
        }
    });
}


/**
 * Apply a PBR texture to a material. Every field of PBR texture that contain data will be applied to the material
 *
 * @param material the material of the object
 * @param textures the PBR textures containing the individual textures
 * @private
 */
function applyPBRTexture(material: MeshStandardMaterial, textures: PBRTextures) {
    // clear any existing maps first
    material.map = null;
    material.aoMap = null;
    material.normalMap = null;
    material.roughnessMap = null;
    material.metalnessMap = null;
    material.displacementMap = null;
    material.color = new Color(0xFFFFFF);

    if (textures.map?.isTexture) {
        material.map = textures.map;
    }
    if (textures.aoMap?.isTexture) {
        material.aoMap = textures.aoMap;
    }
    // Disabling displacement maps because they don't look correct when the texture is not properly scaled
    if (textures.normalMap?.isTexture) {
        material.normalMap = textures.normalMap;
    }
    if (textures.roughnessMap?.isTexture) {
        material.roughnessMap = textures.roughnessMap;
    }
}