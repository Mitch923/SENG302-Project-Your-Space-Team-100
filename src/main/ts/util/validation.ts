import {countGraphemeClusters} from "@/edit-design-page-controls";
import {removeFailToast, showFailToast} from "./toastNotifications";

const nameEmptyMessage = "Design name cannot be empty";
const nameTooLongMessage = "Design name must be 255 characters or less";
const descriptionTooLongMessage = "Design description must be 512 characters or less";
const scaleIncorrectMessage = "Object's scale must be between -100 and -0.01, or 0.01 and 100";
const nameErrorElement = document.getElementById("designNameError") as HTMLParagraphElement;
const descriptionErrorElement = document.getElementById("designDescriptionError") as HTMLParagraphElement;
const nameInputElement = document.getElementById("designName") as HTMLInputElement;
const descriptionInputElement = document.getElementById("designDescription") as HTMLTextAreaElement;
const charCounterElement = document.getElementById("charCount") as HTMLSpanElement;
const scaleInputElement = document.getElementById("scale-input") as HTMLInputElement;

/**
 * Validate if name is empty and apply/remove the error message.
 *
 * @returns {boolean} true if validation succeeds.
 */
function validateDesignName(): boolean {
    let isValid = true;
    const name = nameInputElement.value;
    if (name.trim() === "") {
        nameErrorElement.innerText = nameEmptyMessage;
        nameInputElement.classList.add("is-invalid");
        isValid = false;
    } else if (countGraphemeClusters(name) > 255) {
        nameErrorElement.innerText = nameTooLongMessage;
        nameInputElement.classList.add("is-invalid");
        isValid = false;
    } else {
        nameErrorElement.innerText = '';
        nameInputElement.classList.remove("is-invalid");
    }
    return isValid;
}

/**
 * Validate if description is too long, using graphme to account for emojis,
 * and apply/remove the error message.
 *
 * @returns {boolean} true if validation succeeds.
 */
function validateDesignDescription(): boolean {
    let isValid = true;
    const description = descriptionInputElement.value;
    if (countGraphemeClusters(description) > 512) {
        descriptionErrorElement.innerText = descriptionTooLongMessage;
        descriptionInputElement.classList.add("is-invalid");
        charCounterElement.classList.add("text-danger");
        isValid = false;
    } else {
        descriptionErrorElement.innerText = '';
        descriptionInputElement.classList.remove("is-invalid");
        charCounterElement.classList.remove("text-danger");
    }
    return isValid;
}

/**
 * Validate scale is in the allowed range, and show error toast if it is not.
 *
 * @param scale {number} scale value to be applied to an object.
 * @returns {boolean} true if validation succeeds.
 */
function validateObjectScale(scale: string): boolean {
    if (scale.trim() === '') {
        // Remove red outline/error toast to not confuse user,
        // but return false so object scale doesn't get updated.
        scaleInputElement.classList.remove("is-invalid");
        removeFailToast();
        return false;
    }
    if (isNaN(Number(scale))) {
        showFailToast(scaleIncorrectMessage);
        scaleInputElement.classList.add("is-invalid");
        return false;
    }
    const scaleNum = Number(scale);
    const inUpperRange = scaleNum >= 0.01 && scaleNum <= 100;
    const inLowerRange = scaleNum <= -0.01 && scaleNum >= -100;
    const isValid = inUpperRange || inLowerRange;
    if (!isValid) {
        showFailToast(scaleIncorrectMessage);
        scaleInputElement.classList.add("is-invalid");
        return false;
    } else {
        scaleInputElement.classList.remove("is-invalid");
        removeFailToast();
        return true;
    }
}

/**
 * Takes in a file and validates by simple size and format criteria
 * @param file file object obtained via fileInputElement.files[0]
 * @returns {boolean} true if valid false if not
 */
export function validateParallelTextureImage(file: File) {
    // validate image is <= 10MB
    const maxFileSize = 10; // MB
    const fileSizeInMB = file.size / (1000 * 1000);
    const acceptedTypes = ["image/png", "image/jpeg"];
    if (!acceptedTypes.includes(file.type) && fileSizeInMB > maxFileSize) {
        showFailToast(
                "Texture file must be of type png or jpg. File upload must be less than 10MB");
        return false;
    } else if (!acceptedTypes.includes(file.type)) {
        showFailToast("Texture file must be of type png or jpg.");
        return false;
    } else if (fileSizeInMB > maxFileSize) {
        showFailToast("File upload must be less than 10MB");
        return false;
    }
    return true;
}

export {validateDesignName, validateDesignDescription, validateObjectScale}