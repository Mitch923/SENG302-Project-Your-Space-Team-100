import * as THREE from "three";
import {Object3D, Vector3} from "three";
import {SceneObjectSelector} from "@/objectcontrols/SceneObjectSelector";
import {removeObject} from "@/objectcontrols/objectManipulator";
import {ModelCounter} from "./ModelCounter";
import {TransformControlsMode} from "three/examples/jsm/controls/TransformControls";
import {validateObjectScale} from "./validation";
import {updateTextureCardBackground} from "@/edit-design-page-controls";

const menuElement = document.getElementById("object-details-menu") as HTMLElement;
const tabButtons = document.getElementById("object-details-tab-buttons") as HTMLElement;
const objectDetailsBox = document.getElementById("object-details-box") as HTMLElement;
const objectControlsButton = document.getElementById("object-controls-btn") as HTMLElement;
const lightingControlsButton = document.getElementById("lighting-controls-btn") as HTMLElement;
const objectControlsTab = document.getElementById("object-controls-tab") as HTMLElement;
const lightingControlsTab = document.getElementById("lighting-controls-tab") as HTMLElement;
const objectNameElement = document.getElementById("object-name") as HTMLElement;
const objectCoordinatesElementX = document.getElementById("object-coordinates-x") as HTMLElement;
const objectCoordinatesElementY = document.getElementById("object-coordinates-y") as HTMLElement;
const objectCoordinatesElementZ = document.getElementById("object-coordinates-z") as HTMLElement;
const colourPickerIconElement = document.getElementById("colour-picker-icon") as SVGElement | null;
const colourPickerElement = document.getElementById("colour-picker") as HTMLInputElement;
const lightIntensityElement = document.getElementById("intensity-slider") as HTMLInputElement;
const lightWarmthElement = document.getElementById("warmth-slider") as HTMLInputElement;
const rotateInput = document.getElementById("vbtn-rotate") as HTMLInputElement;
const rotateLabel = document.getElementById("rotate-label") as HTMLLabelElement;
const translateInput = document.getElementById("vbtn-translate") as HTMLInputElement;
const translateLabel = document.getElementById("translate-label") as HTMLLabelElement;
const rotateObjectDivElement = document.getElementById("rotate-object-div") as HTMLDivElement;
const scaleInputElement = document.getElementById("scale-input") as HTMLInputElement;
const scaleDivElement = document.getElementById("scale-div") as HTMLDivElement;
const removeButton = document.getElementById("removeObjectButton") as HTMLButtonElement;

export function setupRemoveButtonListener(objectSelector: SceneObjectSelector, modelCounter: ModelCounter) {
    removeButton.addEventListener("click", () => {
        const obj = objectSelector.getSelectedObject();
        if (obj) {
            const scene = objectSelector.getScene();
            if (scene) {
                removeObject(scene, obj, modelCounter);
                objectSelector.deselect();
                showObjectDetailsMenu(false, null);
            }
        }
    });
}

const snapButton = document.getElementById('snapToFloorBtn') as HTMLElement;

/**
 * Show / hide the object details menu.
 *
 * @param showMenu {boolean} true to show the menu, false to hide it.
 * @param object {THREE.Object3D} currently selected object.
 */
export function showObjectDetailsMenu(showMenu: boolean, object: THREE.Object3D | null) {
    if (showMenu) {
        menuElement.classList.remove("d-none");
        tabButtons.classList.remove("d-none");
    } else {
        menuElement.classList.add("d-none");
        tabButtons.classList.add("d-none");
    }
    if (showMenu && object) {
        objectNameElement.innerText = object.userData.displayName;
        if (object.userData.displayName && object.userData.displayName.trim().length == 0) {
            objectNameElement.innerText = "Unnamed object";
        }
        removeButton.hidden = !object?.userData.movable; // remove 'delete' button if not movable
        updateCoordinates(object);
        snapButton.hidden = !object.userData.movable;
        showScale(object.userData.movable);
        showRotateButtonGroup(object.userData.movable);
        if (object.userData.isLight) {
            showLightingControlsTab();
        } else {
            hideLightingControlsTab();
        }
    }
}

function showLightingControlsTab() {
    tabButtons.classList.remove("d-none");
    objectDetailsBox.classList.add("no-rounded-top-left-corner");
}

function hideLightingControlsTab() {
    tabButtons.classList.add("d-none");
    objectControlsTab.classList.add("show");
    objectControlsTab.classList.add("active");
    objectControlsButton.classList.add("active");
    lightingControlsTab.classList.remove("show");
    lightingControlsTab.classList.remove("active");
    lightingControlsButton.classList.remove("active");
    objectDetailsBox.classList.remove("no-rounded-top-left-corner");
}

/**
 * Update the coordinates of the currently selected object on the object details menu.
 *
 * @param object {THREE.Object3D} currently selected object.
 */
export function updateCoordinates(object: THREE.Object3D) {
    const coordinates = object.getWorldPosition(new Vector3());
    objectCoordinatesElementX.innerText = `X: ${(coordinates.x * 100).toFixed(2)}`;
    objectCoordinatesElementY.innerText = `Y: ${(coordinates.y * 100).toFixed(2)}`;
    objectCoordinatesElementZ.innerText = `Z: ${(coordinates.z * 100).toFixed(2)}`;
}

/**
 * Change the colour of the colour picker icon.
 *
 * @param colour Hex colour value
 */
export function updateColourPickerIcon(colour: string) {
    if (colourPickerIconElement) {
        colourPickerIconElement.setAttribute('fill', colour);
    }
}

/**
 * Change the selected colour of the colour picker input.
 *
 * @param colour Hex colour value
 */
export function updateColourPickerValue(colour: string) {
    colourPickerElement.value = colour;
}

/**
 * Set event listener on the colour picker input element which uses callback functions to get
 * the currently selected object and change its colour.
 *
 * @param currentlySelectedObject function that returns the currently selected object.
 * @param changeObjectColour A callback function that applies a colour to a given object.
 */
export function setColourPickerEventListener(currentlySelectedObject: () => Object3D | null, changeObjectColour: (object: Object3D, colour: string) => void) {
    colourPickerElement.addEventListener("input", () => {
        updateColourPickerIcon(colourPickerElement.value);
        updateTextureCardBackground(colourPickerElement.value);
        const object = currentlySelectedObject();
        if (object) {
            changeObjectColour(object, colourPickerElement.value);
        }
    });
}

/**
 * Set event listener on the light intensity input element which uses callback functions to get the
 * currently selected object and change its light intensity
 *
 * @param getCurrentlySelectedObject function that returns the currently selected object
 * @param changeObjectIntensity callback function that applies the light intensity to a given object
 */
export function setLightIntensityEventListener(getCurrentlySelectedObject: () => Object3D | null, changeObjectIntensity: (object: Object3D, intensity: number) => void) {
    lightIntensityElement.addEventListener("input", () => {
        const object = getCurrentlySelectedObject();
        if (object) {
            changeObjectIntensity(object, parseFloat(lightIntensityElement.value))
        }
    })
}

export function setLightWarmthEventListener(getCurrentlySelectedObject: () => Object3D | null, changeObjectWarmth: (object: Object3D, value: number) => void) {
    lightWarmthElement.addEventListener("input", () => {
        const object = getCurrentlySelectedObject();
        if (object) {
            changeObjectWarmth(object, parseFloat(lightWarmthElement.value));
        }
    })
}

/**
 * Sets the event listener on the transform controls button group which will toggle the state of the
 * transform controls between 'rotate' and 'translate'.
 *
 * @param toggleTransformControls The callback from the transform controls that will change the mode.
 */
export function setRotateObjectEventListener(toggleTransformControls: (mode: TransformControlsMode) => void) {
    rotateInput.addEventListener("change", () => {
        const mode = rotateInput.checked ? 'rotate' : 'translate';
        rotateLabel.classList.add("fw-bold");
        translateLabel.classList.remove("fw-bold");
        toggleTransformControls(mode);
    });
    translateInput.addEventListener("change", () => {
        const mode = translateInput.checked ? 'translate' : 'rotate';
        translateLabel.classList.add("fw-bold");
        rotateLabel.classList.remove("fw-bold");
        toggleTransformControls(mode);
    });
}

/**
 * Show/hide the rotation / translation buttons from the object details menu. Used for when user
 * clicks on an unmovable object eg Wall and Floor.
 *
 * @param showSwitch {boolean} true to show switch, false to hide.
 */
function showRotateButtonGroup(showSwitch: boolean) {
    if (showSwitch) {
        rotateObjectDivElement.classList.remove("d-none");
    } else {
        rotateObjectDivElement.classList.add("d-none");
    }
}

/**
 * Sets event listener on scale input element to pass the user's entered scale value to the
 * function that modifies the object.
 *
 * @param currentlySelectedObject function that returns the currently selected object.
 * @param setObjectScale function to modify the objects scale.
 */
export function setScaleEventListener(currentlySelectedObject: () => Object3D | null, setObjectScale: (object: Object3D, scale: number) => void) {
    scaleInputElement.addEventListener('input', () => {
        const object = currentlySelectedObject();
        if (object && validateObjectScale(scaleInputElement.value)) {
            setObjectScale(object, parseFloat(scaleInputElement.value));
        }
    });
}

/**
 * Show / hide the scale input, can be used when the user clicks on an unmovable
 * object e.g. Wall / Floor.
 *
 * @param show {boolean} true to show scale input, false to hide.
 */
function showScale(show: boolean) {
    if (show) {
        scaleDivElement.classList.remove("d-none");
    } else {
        scaleDivElement.classList.add("d-none");
    }
}

/**
 * Update the value of the scale object input.
 *
 * @param scale {number} scale value
 */
export function updateScaleValue(scale: number) {
    scaleInputElement.value = `${scale}`;
}

/**
 * Sets an event listener on the snap to floor button that runs the supplied callback
 * @param snapToFloorCallback function to execute when click event detected
 */
export function setSnapToFloorEventListener(snapToFloorCallback: () => void) {
    const snapButton = document.getElementById('snapToFloorBtn');
    if (snapButton) {
        snapButton.addEventListener('click', snapToFloorCallback);
    }
}

export function updateIntensityScaleValue(lightIntensity: number) {
    lightIntensityElement.value = `${lightIntensity}`;
}

export function updateWarmthScaleValue(value: number) {
    lightWarmthElement.value = `${value}`;
}
