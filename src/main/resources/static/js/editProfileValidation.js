document.addEventListener('DOMContentLoaded', function () {

});

/**
 * Maps the click event of the edit profile button to opening the file picker
 */
function onEditProfilePictureClicked() {
    const fileInputElement = document.getElementById("file-input");

    fileInputElement.click();
}

// variable for keeping track of if the last uploaded file is valid or not
let fileValid = !document.getElementById("imageUploadSsErr");

/**
 * Triggered when the file input gets changed. Updates the icon preview and
 * calls the method to perform front end validation.
 */
function onFileInputChanged() {
    const fileInputElement = document.getElementById("file-input");
    const chosenFileNameElement = document.getElementById("chosen-file-name");
    const imageSourceData = document.getElementById("currentPfp");
    const currentPfp = imageSourceData.dataset.profileimagepath;
    const useFailSubmissionImage = document.getElementById(
            "use-fail-submission-image");

    let reader = new FileReader();

    const pfpElementToUse = getImageElementToUse();

    // Set the image when the file is loaded
    reader.onload = function (e) {
        pfpElementToUse.src = e.target.result;
    };
    if (fileInputElement.files.length > 0) {
        const chosenFile = fileInputElement.files[0];
        useFailSubmissionImage.value = "false";
        if (validateFileInput(chosenFile)) {  // Only proceed if file is valid
            chosenFileNameElement.textContent = chosenFile.name;
            reader.readAsDataURL(chosenFile);
        } else {
            // Revert to current profile image if the file is not valid
            pfpElementToUse.src = currentPfp;
            chosenFileNameElement.textContent = chosenFile.name;
        }
    } else {
        // If no file selected, revert to current profile image
        pfpElementToUse.src = currentPfp;
        chosenFileNameElement.textContent = "";
    }
}

/**
 * Gets the <p> tag that displays image validation errors and makes it visible
 * with appropriate style and message. Adds a red border to the invalid
 * @param message string that is inserted into the error message <p>
 */
const showFileUploadError = (message) => {
    const serverSideError = document.getElementById("imageUploadSsErr");
    const messageElement = document.getElementById("imageValidationError");
    const chosenFileNameElement = document.getElementById("chosen-file-name");

    if (serverSideError) {
        serverSideError.remove(); // stops displaying double error message if server side one pre-exists
    }

    messageElement.style.display = "block";
    messageElement.classList.add("text-center", "text-lg-start",
            "text-md-center");
    messageElement.textContent = message;

    chosenFileNameElement.classList.add("text-danger",
            "text-decoration-underline");

    const imageElement = getImageElementToUse();
    imageElement.classList.add("image-error");
}

/**
 * Gets the <p> tag that displays image validation errors and hides it. Removes
 * the red border from the profile picture
 */
const hideFileUploadError = () => {
    const messageElement = document.getElementById("imageValidationError");
    const messageElementSS = document.getElementById("imageUploadSsErr");
    const chosenFileNameElement = document.getElementById("chosen-file-name");

    messageElement.classList.remove("text-center", "text-lg-start",
            "text-md-center");
    messageElement.style.display = "none";
    messageElement.textContent = "";

    if (messageElementSS) {
        messageElementSS.classList.remove("text-center", "text-lg-start",
                "text-md-center");
        messageElementSS.style.display = "none";
        messageElementSS.textContent = "";
    }

    chosenFileNameElement.classList.remove("text-danger",
            "text-decoration-underline");

    const imageElement = getImageElementToUse();
    imageElement.classList.remove("image-error");
}

/**
 * Takes in a file and validates by simple size and format criteria
 * @param file file object obtained via fileInputElement.files[0]
 * @returns {boolean} true if valid false if not
 */
const validateFileInput = (file) => {
    // validate image is <= 10MB
    const maxFileSize = 10; // MB
    const fileSizeInMB = file.size / (1024 * 1024);
    const acceptedTypes = ["image/png", "image/jpeg", "image/svg+xml"];
    if (!acceptedTypes.includes(file.type) && fileSizeInMB > maxFileSize) {
        showFileUploadError(
                "Image must be of type png, jpg or svg. Image must be less than 10MB");
        fileValid = false;
        return false;
    } else if (!acceptedTypes.includes(file.type)) {
        showFileUploadError("Image must be of type png, jpg or svg");
        fileValid = false;
        return false;
    } else if (fileSizeInMB > maxFileSize) {
        showFileUploadError("Image must be less than 10MB");
        fileValid = false;
        return false;
    }
    // check if the fileValid is false (meaning it has been validated before but failed, i.e. an error message will be shown)
    if (!fileValid) {
        fileValid = true;
        hideFileUploadError(); // hide the error message once the image has been validated
    }
    return true;
}

/**
 * Figures out which profile picture element is currently in use and returns it
 *
 * @returns {HTMLElement} the pfp element currently in use
 */
function getImageElementToUse() {
    const pfpImage = document.getElementById("pfp-image");
    const pfpImageDefault = document.getElementById("pfp-image-default");
    const pfpImageFailedReq = document.getElementById("pfp-image-failed-req");
    const pfpImagePrevFailedReq = document.getElementById(
            "pfp-image-prev-failed-req");

    let pfpElementToUse;
    if (pfpImage) {
        pfpElementToUse = pfpImage;
    } else if (pfpImageDefault) {
        pfpElementToUse = pfpImageDefault;
    } else if (pfpImageFailedReq) {
        pfpElementToUse = pfpImageFailedReq;
    } else {
        pfpElementToUse = pfpImagePrevFailedReq;
    }

    return pfpElementToUse;
}