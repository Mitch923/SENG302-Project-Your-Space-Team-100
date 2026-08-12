import {Mesh, Object3D} from "three";

/**
 * Does a "deeper clone" of the object including cloning materials, so that when
 * the original object's materials are modified, they are not referenced by the clone
 * and thus do not get accidentally modified at the same time.
 * @param object
 * @return Object3D - a fully cloned object with separate materials that do not reference the old object.
 */
export function cloneObjectWithMaterials(object: Object3D): Object3D {
    const clone = object.clone();

    clone.traverse((child) => {
        if ((child as Mesh).isMesh) {
            const mesh = child as Mesh;
            if (Array.isArray(mesh.material)) {
                mesh.material = mesh.material.map(material => material.clone());
            } else if (mesh.material) {
                mesh.material = mesh.material.clone();
            }
        }
    });

    return clone;
}