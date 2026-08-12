import {Color, Light, Object3D} from "three";

/**
 * Change the update any lights contained in the given object to have the given intensity
 *
 * @param object whose lights to update
 * @param lightIntensity value to update the intensity to
 */
export function changeObjectLightIntensity(object: Object3D, lightIntensity: number) {
    object.traverse(child => {
        if (child instanceof Light) {
            child.intensity = lightIntensity;
        }
    });
    object.userData.lightIntensity = lightIntensity;
}

/**
 * Change the update any lights contained in the given object to have the given intensity
 *
 * @param object whose lights to update
 * @param value
 */
export function changeObjectWarmth(object: Object3D, value: number) {
    object.traverse(child => {
        if (child instanceof Light) {
            child.color = new Color(convertPercentageToLightWarmth(value));
        }
    });
    object.userData.warmth = value;
}


function convertPercentageToLightWarmth(value: number) {
    const orange = new Color(0xFFBC00);
    const yellow = new Color(0xFFFC99);
    const white = new Color(0xFFFFFF);
    const blue = new Color(0xE5FFFD);

    let c1, c2, t;

    if (value < 30) {
        t = value / 30;
        c1 = blue;
        c2 = white;
    } else if (value < 70) {
        t = (value - 30) / 40;
        c1 = white;
        c2 = yellow;
    } else {
        t = (value - 70) / 30;
        c1 = yellow;
        c2 = orange;
    }

    return c1.clone().lerp(c2, t);
}