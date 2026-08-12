import {SceneObjectSelector} from "./SceneObjectSelector";
import {ColourPicker} from "./ColourPicker";
import {
    setColourPickerEventListener,
    setLightIntensityEventListener,
    setLightWarmthEventListener,
    setRotateObjectEventListener,
    setScaleEventListener,
    setSnapToFloorEventListener,
    setupRemoveButtonListener,
    showObjectDetailsMenu,
    updateColourPickerIcon,
    updateColourPickerValue,
    updateCoordinates,
    updateIntensityScaleValue,
    updateScaleValue,
    updateWarmthScaleValue
} from "@/util/objectDetailsMenu";
import {scaleObject, snapObjectToFloor} from "./objectManipulator";
import {ModelCounter} from "@/util/ModelCounter";
import {TransformControlsMode} from "three/examples/jsm/controls/TransformControls";
import {updateTextureCardBackground} from "@/edit-design-page-controls";
import {changeObjectLightIntensity, changeObjectWarmth} from "@/objectcontrols/LightingControls";

/**
 * This class is a little bit redundant now because I have refactored SceneObjectSelector to
 * be singleton so getSelectedObject() can now be called from anywhere.
 *
 * But I do not want to refactor everything right now, so I'm leaving this as is.
 *
 * Central coordinator for object interaction logic in the scene.
 * The ObjectController is responsible for mediating between UI events, object selection,
 * and object manipulation (e.g. colour changes via ColourPicker).
 */
export class ObjectController {

    private readonly sceneObjectSelector: SceneObjectSelector;
    private colourPicker: ColourPicker;

    constructor(modelCounter: ModelCounter) {
        this.sceneObjectSelector = SceneObjectSelector.getInstance();
        this.colourPicker = new ColourPicker();
        this.setColourControls();
        this.setObjectSelectorCallback();
        this.setTransformControls();
        this.setSnapControls();
        this.setLightIntensityControls();
        setupRemoveButtonListener(this.sceneObjectSelector, modelCounter);
        this.setRotationSwitchCallback();
        this.setObjectScaleCallback();
    }

    /**
     * Set the event listener to get the users selected colour and apply it to the currently
     * selected object.
     * @private
     */
    private setColourControls() {
        setColourPickerEventListener(
                () => this.sceneObjectSelector.getSelectedObject(),
                (object, colour) => this.colourPicker.changeObjectColour(object, colour)
        );
    }

    /**
     * Set the event listener to get the value from the intensity slider and apply it to the currently
     * selected light object
     * @private
     */
    private setLightIntensityControls() {
        setLightIntensityEventListener(
                () => this.sceneObjectSelector.getSelectedObject(),
                (object, intensity) => changeObjectLightIntensity(object, intensity)
        )
        setLightWarmthEventListener(() => this.sceneObjectSelector.getSelectedObject(),
                (object, value) => changeObjectWarmth(object, value))
    }

    /**
     * Add callback functions to SceneObjectSelector when an object is selected or deselected.
     * (This used to be on editor.ts).
     * Add/remove transform controls, update object menu elements.
     * @private
     */
    private setObjectSelectorCallback() {
        const scene = this.sceneObjectSelector.getScene();
        if (scene) {
            this.sceneObjectSelector.setOnSelect(object => {
                if (object.userData.movable) {
                    scene.transformControls.attach(object);
                    scene.transformControls.getHelper().visible = true;
                } else {
                    scene.transformControls.detach();
                    scene.transformControls.getHelper().visible = false;
                }
                updateScaleValue(object.userData.scale);
                updateIntensityScaleValue(object.userData.lightIntensity);
                updateWarmthScaleValue(object.userData.warmth);
                updateColourPickerValue(object.userData.colour);
                updateColourPickerIcon(object.userData.colour);
                showObjectDetailsMenu(true, object);
                updateTextureCardBackground(object.userData.colour);
            });

            this.sceneObjectSelector.setOnDeselect(obj => {
                scene.transformControls.detach();
                scene.transformControls.getHelper().visible = false;
                showObjectDetailsMenu(false, null);
                updateTextureCardBackground('#FFFFFF');
            });
        }
    }

    /**
     * Set event listener on the transform controls so that the objects coordinates update
     * as the user moves it around the scene.
     * @private
     */
    private setTransformControls() {
        const scene = this.sceneObjectSelector.getScene();
        if (scene) {
            scene.transformControls.addEventListener("change", () => {
                const object = this.sceneObjectSelector.getSelectedObject();
                if (object) {
                    updateCoordinates(object);
                }
            });
        }
    }

    /**
     * Set event listener to get the users input when they switch the controls from translate to
     * rotation / rotation to translate mode.
     * @private
     */
    private setRotationSwitchCallback() {
        const scene = this.sceneObjectSelector.getScene();
        if (scene) {
            setRotateObjectEventListener((mode: TransformControlsMode) => scene.transformControls.setMode(mode));
        }
    }

    /**
     * Sets the event listener to scale the object wh
     * @private
     */
    private setObjectScaleCallback() {
        setScaleEventListener(() => this.sceneObjectSelector.getSelectedObject(),
                (object, scale) => scaleObject(object, scale));
    }


    /**
     * Sets the event listener on the snap to floor button using the method in objectDetailsMenu module
     * @private
     */
    private setSnapControls() {
        setSnapToFloorEventListener(() => {
            const selectedObject = this.sceneObjectSelector.getSelectedObject();
            if (selectedObject) {
                snapObjectToFloor(
                        selectedObject
                );
                updateCoordinates(selectedObject);
            }
        });
    }
}