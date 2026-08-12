/**
 * Logic and helper functions for location autofill
 */
let user_location = undefined;
let debounce_timeout;
const locationAutocompleteList = document.getElementById("autocomplete-list");
const locationSelectBtn = document.getElementById('location-select-btn');
const locationSelectBtnTitle = document.getElementById(
        'selected-location-title');
const locationSelectBtnSubtitle = document.getElementById(
        'selected-location-subtitle');
const selectedLocationIndicator = document.getElementById(
        'selected-location-modal-box');
const selectedLocationIndicatorText = selectedLocationIndicator.querySelector(
        'p');
const streetInput = document.getElementById("streetAddressField")
const suburbInput = document.getElementById("suburbField")
const cityInput = document.getElementById("cityField")
const postcodeInput = document.getElementById("postcodeField")
const countryInput = document.getElementById("countryField")

// For the hidden <input>s that will be submitted with the form
const streetSubmission = document.getElementById("streetAddress")
const suburbSubmission = document.getElementById("suburb")
const citySubmission = document.getElementById("city")
const postcodeSubmission = document.getElementById("postcode")
const countrySubmission = document.getElementById("country")

/**
 * An array of input fields that make up the full location form.
 * These fields are monitored for user input to trigger address autocomplete.
 */
const locationInputs = [streetInput, suburbInput, cityInput, postcodeInput,
    countryInput];

/**
 * Adds an input event listener to the provided field that triggers
 * location autocomplete with a debounce delay.
 *
 * Debouncing ensures that the Mapbox API is not called on every keystroke,
 * but only after the user has stopped typing for 250 milliseconds.
 * This improves performance and reduces unnecessary API requests.
 *
 * @param {HTMLInputElement} input - The input element to attach the listener to.
 */
function addDebouncedListener(input) {
    input.addEventListener("input", () => {
        clearTimeout(debounce_timeout);
        debounce_timeout = setTimeout(updateLocationAutocompleteDropdown, 250);
    });
    input.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault()
        }
    })
}

locationInputs.forEach(addDebouncedListener);

/**
 * When the user opens the location modal, the input fields will be populated
 * with previously selected location.
 */
function clickOpenLocationModal() {
    const submissionForm = [streetSubmission.value, suburbSubmission.value,
        citySubmission.value, postcodeSubmission.value, countrySubmission.value]
    if (submissionForm.filter(f => (f !== undefined && f !== "")).length
            !== 0) {
        clearLocationAutocompleteDropdown()
        streetInput.value = streetSubmission.value || ""
        suburbInput.value = suburbSubmission.value || ""
        cityInput.value = citySubmission.value || ""
        postcodeInput.value = postcodeSubmission.value || ""
        countryInput.value = countrySubmission.value || ""
    }
}

/**
 * When the user clicks the button which opens the location modal,
 * at that point get the user's location.
 */
document.getElementById("location-select-btn").addEventListener("click",
        async () => {
            user_location = await getUserLatLon();
        });

/**
 * Event listener to detect click on any of the child elements of the autofill location list,
 * and then execute steps to autofill the respective fields
 */
locationAutocompleteList.addEventListener('click', (event) => {
    const childClicked = event.target.closest(".list-group-item");
    if (childClicked && locationAutocompleteList.contains(childClicked)
            && childClicked.dataset.default_message !== "true") {
        streetInput.value = childClicked.dataset.street
        suburbInput.value = childClicked.dataset.suburb
        cityInput.value = childClicked.dataset.city
        postcodeInput.value = childClicked.dataset.postcode
        countryInput.value = childClicked.dataset.country
        clearLocationAutocompleteDropdown()
        addDefaultMessage()
    }
})

/**
 * Use this function to change the location selector color to indicate valid status
 */
const setLocationValid = () => {
    // modal location indicator
    selectedLocationIndicator.classList.remove('location-invalid',
            'location-inactive');
    selectedLocationIndicator.classList.add('location-valid');
    // select location button
    locationSelectBtn.classList.remove('location-invalid', 'location-inactive');
    locationSelectBtn.classList.add('location-valid');
    locationSelectBtnSubtitle.innerHTML = "";
}

/**
 * Use this function to change the location selector color to indicate invalid status
 */
const setLocationInvalid = () => {
    // modal location indicator
    selectedLocationIndicator.classList.remove('location-valid',
            'location-inactive');
    selectedLocationIndicator.classList.add('location-invalid');
    // select location button
    locationSelectBtn.classList.remove('location-valid', 'location-inactive');
    locationSelectBtn.classList.add('location-invalid');
    locationSelectBtnSubtitle.innerHTML = "Invalid address.";
}

/**
 * Use this function to change the location selector color to indicate inactive status
 * This will also change the location selected to display "select location"
 */
const setLocationInactive = () => {
    // modal location indicator
    selectedLocationIndicator.classList.remove('location-invalid',
            'location-valid');
    selectedLocationIndicator.classList.add('location-inactive');
    // select location button
    locationSelectBtn.classList.remove('location-invalid', 'location-valid');
    locationSelectBtn.classList.add('location-inactive');
    // display select location
    locationSelectBtnTitle.innerHTML = 'Select Location';
    locationSelectBtnSubtitle.innerHTML = 'Add a location here';
    selectedLocationIndicatorText.innerHTML = 'No Location';
}

/**
 * Gets the user's current input from all input fields (street, suburb, postcode
 * , city, country) and joins them together to be used as the query string
 * that is sent to Mapbox's API.
 * Fields that have not been filled out by the user are not included.
 * @returns {string} The full address
 */
function getUserInput() {
    const user_input_street = streetInput.value.trim()
    const user_input_suburb = suburbInput.value.trim()
    const user_input_city = cityInput.value.trim()
    const user_input_postcode = postcodeInput.value.trim()
    const user_input_country = countryInput.value.trim()
    const address_parts = [user_input_street, user_input_suburb,
        user_input_city, user_input_postcode, user_input_country]
    .filter(p => p !== "")
    return address_parts.join(", ");
}

/**
 * Sends a request to a proxy for Mapbox's Geocode API to get autocomplete location results
 * based off a users current input.
 * @param query The user's current location input
 * @returns {Promise<undefined|any>} undefined or JSON response from Mapbox
 */
async function fetchLocationAutocompleteItems(query) {
    if (query.trim() === "") {
        return undefined;
    }
    const queryUriEncoded = encodeURIComponent(query.trim());
    const url = `getMapboxForwardGeocoding?query=${queryUriEncoded}&userLocation=${user_location}`;
    try {
        const response = await fetch(url);
        if (response.ok) {
            const jsonData = await response.json();
            return jsonData;
        } else {
            throw new Error(`Response status not ok: ${response.status}`);
        }
    } catch (error) {
        console.error('Error fetching location autocomplete data: ', error);
        return undefined;
    }
}

/**
 * Sends a request to the ipapi proxy which returns the users IP address and location
 * details based off their IP address (which does not require user permission)
 * @returns {Promise<undefined|any>} undefined or the JSON Response from ipapi
 */
async function fetchIPGeolocation() {
    try {
        const response = await fetch("getIPGeolocation");
        if (response.ok) {
            return await response.json();
        } else {
            throw new Error(`Response status not ok: ${response.status}`);
        }
    } catch (error) {
        console.error('Error fetching ip geolocation: ', error);
        return undefined;
    }
}

/**
 * Extract the user's approximate latitude and longitude coordinates from
 * ipapi's repsonse and formats it correctly to be used in a request to Mapbox.
 * @returns {Promise<string>} user location formatted like 'longitude,latitude'
 */
async function getUserLatLon() {
    const jsonData = await fetchIPGeolocation();
    return `${jsonData.longitude},${jsonData.latitude}`;
}

/**
 * Calls the function to fetch autocomplete results from Mapbox
 * and updates the dropdown div elements based off any received results.
 * @returns {Promise<void>} returns nothing
 */
async function updateLocationAutocompleteDropdown() {
    clearLocationAutocompleteDropdown();
    addDefaultMessage();
    const query = getUserInput()
    const jsonData = await fetchLocationAutocompleteItems(query);
    if (!(jsonData === undefined) && jsonData.features.length !== 0) {
        clearLocationAutocompleteDropdown();
        jsonData.features.forEach(item => {
            const street = [item.properties.context.address?.address_number
            || "",
                item.properties.context.street?.name || ""].filter(
                    a => a !== "").join(" ") || ""
            const suburb = item.properties.context.locality?.name || ""
            const city = item.properties.context.place?.name || ""
            const postcode = item.properties.context.postcode?.name || ""
            const country = item.properties.context.country?.name || ""
            addLocationAutocompleteItem(item.properties.full_address, street,
                    suburb, city, postcode, country)
        });
    }
}

/**
 * Adds an element to the autocomplete area representing an autocomplete
 * option. Sets data-* attributes for each address part (eg street, city etc)
 * which are used to autofill the form.
 * @param full_address The whole address to display to the user
 * @param street To be saved for autofill
 * @param suburb To be saved for autofill
 * @param city To be saved for autofill
 * @param postcode To be saved for autofill
 * @param country To be saved for autofill
 */
function addLocationAutocompleteItem(full_address, street, suburb, city,
        postcode, country) {
    const autocompleteItem = document.createElement("li");
    autocompleteItem.classList.add('list-group-item', 'bg-transparent');
    autocompleteItem.textContent = full_address;
    autocompleteItem.dataset.street = street; // note, JS will convert this to a data-* attribute on the <li> element with hyphen-style instead of camelCase
    autocompleteItem.dataset.suburb = suburb;
    autocompleteItem.dataset.city = city;
    autocompleteItem.dataset.postcode = postcode;
    autocompleteItem.dataset.country = country;
    locationAutocompleteList.appendChild(autocompleteItem);
}

/**
 * Add the default element to the autocomplete area if no autocomplete
 * results are available.
 */
function addDefaultMessage() {
    const defaultMessage = document.createElement("li");
    defaultMessage.classList.add('list-group-item', 'bg-transparent');
    defaultMessage.textContent = "No location suggestions are available.";
    defaultMessage.dataset.default_message = "true";
    locationAutocompleteList.appendChild(defaultMessage);
}

/**
 * Clear all elements from the location autocomplete dropdown div.
 */
function clearLocationAutocompleteDropdown() {
    locationAutocompleteList.innerHTML = "";
}

/**
 * Used by the save button, sets the location indicator value to the address
 * that the user has input, and calls the validation functions.
 * If the validation passes, locationFieldState is updated with the address.
 * If the validation fails, locationFieldState is cleared.
 */
const clickSaveButton = () => {
    // Update the indicative buttons with user input address
    const fullAddress = [streetInput.value || "",
        suburbInput.value || "", cityInput.value || "",
        postcodeInput.value || "",
        countryInput.value || ""]
    .filter(item => item !== "").join(", ")
    selectedLocationIndicatorText.innerHTML = fullAddress;
    locationSelectBtnTitle.innerHTML = fullAddress;
    // Validate and continue
    const isLocationValid = validateLocationForm()
    if (isLocationValid) {
        saveUserInputToFormSubmission()
        setLocationValid()
        const locationModal = bootstrap.Modal.getInstance(
                document.getElementById('locationModal'))
        locationModal.hide()
    } else {
        clearFormSubmission()
        setLocationInvalid()
    }
}

/**
 * Save the current user input to the hidden input fields which will be submitted with the form.
 */
const saveUserInputToFormSubmission = () => {
    streetSubmission.value = streetInput.value || ""
    suburbSubmission.value = suburbInput.value || ""
    citySubmission.value = cityInput.value || ""
    postcodeSubmission.value = postcodeInput.value || ""
    countrySubmission.value = countryInput.value || ""
}

/**
 * Save the current user input to the hidden input fields which will be submitted with the form.
 */
const clearFormSubmission = () => {
    streetSubmission.value = ""
    suburbSubmission.value = ""
    citySubmission.value = ""
    postcodeSubmission.value = ""
    countrySubmission.value = ""
}

/**
 * Used by Delete Location button, clears the location form and any errors.
 */
const clickRemoveLocation = () => {
    selectedLocationIndicatorText.innerHTML = "";
    locationSelectBtnTitle.innerHTML = "";
    streetInput.value = ''
    suburbInput.value = ''
    cityInput.value = ''
    postcodeInput.value = ''
    countryInput.value = ''
    clearAllErrors()
    setLocationInactive()
    clearLocationAutocompleteDropdown()
    clearFormSubmission()
}

/* <--------------------------- VALIDATION ---------------------------> */

const streetErrorField = document.getElementById("streetError")
const suburbErrorField = document.getElementById("suburbError")
const cityErrorField = document.getElementById("cityError")
const postcodeErrorField = document.getElementById("postcodeError")
const countryErrorField = document.getElementById("countryError")

/**
 * Processes an error message and displays it to the user, or removes any
 * error messages if an empty string is provided.
 * @param errorMessage The error message, or empty string if no error
 * @param errorField The <p> element for the error message
 * @param inputField The <input> element that is invalid
 */
function setErrorMessage(errorMessage, errorField, inputField) {
    if (errorMessage !== "") {
        errorField.textContent = errorMessage
        errorField.style.display = "inline"
        inputField.classList.add("is-invalid")
    } else {
        errorField.style.display = 'none'
        inputField.classList.remove("is-invalid")
    }
}

/**
 * To be used when a form is submitted (eg register a new user) so that the
 * form still submits when the whole location is empty.
 * @returns {boolean} Form empty ?
 */
function isLocationFormEmpty() {
    let streetEmpty = streetInput.value === undefined
            || streetInput.value.trim()
            === ""
    let suburbEmpty = suburbInput.value === undefined
            || suburbInput.value.trim()
            === ""
    let cityEmpty = cityInput.value === undefined || cityInput.value.trim()
            === ""
    let postcodeEmpty = postcodeInput.value === undefined
            || postcodeInput.value.trim() === ""
    let countryEmpty = countryInput.value === undefined
            || countryInput.value.trim() === ""
    return streetEmpty && suburbEmpty && cityEmpty && postcodeEmpty
            && countryEmpty
}

/**
 * Performs validation on the entire location form and sets or removes the
 * appropriate error messages.
 * @returns {boolean} True if the location form is valid, false otherwise
 */
function validateLocationForm() {
    const streetErrorMessage = validateStreet(streetInput.value)
    const suburbErrorMessage = validateSuburb(suburbInput.value)
    const cityErrorMessage = validateCity(cityInput.value)
    const postcodeErrorMessage = validatePostcode(postcodeInput.value)
    const countryErrorMessage = validateCountry(countryInput.value)
    setErrorMessage(streetErrorMessage, streetErrorField, streetInput)
    setErrorMessage(suburbErrorMessage, suburbErrorField, suburbInput)
    setErrorMessage(cityErrorMessage, cityErrorField, cityInput)
    setErrorMessage(postcodeErrorMessage, postcodeErrorField,
            postcodeInput)
    setErrorMessage(countryErrorMessage, countryErrorField, countryInput)
    const errors = [streetErrorMessage, suburbErrorMessage,
        cityErrorMessage, postcodeErrorMessage, countryErrorMessage]
    .filter(e => e !== "")
    return errors.length === 0;
}

/**
 * Validates the street part of the location form.
 * @param street The users input for the street.
 * @returns {string} An error message, or an empty string for no error.
 */
function validateStreet(street) {
    // Remember to escape the hyphen! Means a range in regex!
    const streetRegex = new RegExp(/^[\p{L}\p{M}\d.' \-/]+$/u);
    if (street === undefined || street.trim() === "") {
        return "Street cannot be empty.";
    } else if (!streetRegex.test(street)) {
        return "Street address contains invalid characters.";
    } else if (street.trim().length > 255) {
        return "Street must be less than 256 characters."
    } else {
        return "";
    }
}

/**
 * Validates the suburb part of the location form.
 * @param suburb The users input for the suburb.
 * @returns {string} An error message, or an empty string for no error.
 */
function validateSuburb(suburb) {
    const suburbRegex = new RegExp(/^[\p{L}\p{M}\d' -]+$/u);
    if (!(suburb === undefined || suburb.trim() === "") && !suburbRegex.test(
            suburb)) {
        return "Suburb contains invalid characters.";
    } else if (!(suburb === undefined || suburb.trim() === "")
            && suburb.trim().length > 255) {
        return "Suburb must be less than 256 characters."
    } else {
        return "";
    }
}

/**
 * Validates the city part of the location form.
 * @param city The users input for the city.
 * @returns {string} An error message, or an empty string for no error.
 */
function validateCity(city) {
    const cityRegex = new RegExp(/^[\p{L}\p{M}' -]+$/u);
    if (city === undefined || city.trim() === "") {
        return "City cannot be empty.";
    } else if (!cityRegex.test(city)) {
        return "City contains invalid characters.";
    } else if (city.trim().length > 255) {
        return "City must be less than 256 characters."
    } else {
        return "";
    }
}

/**
 * Validates the postcode part of the location form.
 * @param postcode The users input for the postcode.
 * @returns {string} An error message, or an empty string for no error.
 */
function validatePostcode(postcode) {
    const postcodeRegex = new RegExp(/^[\p{L}\p{M}\d -]+$/u);
    // Postcode is only allowed one space character
    const postcodeHasConsecutiveSpaces = postcode.includes("  ");
    if (postcode === undefined || postcode.trim() === "") {
        return "Postcode cannot be empty.";
    } else if (!postcodeRegex.test(postcode) || postcodeHasConsecutiveSpaces) {
        return "Postcode contains invalid characters.";
    } else if (postcode.trim().length > 255) {
        return "Postcode must be less than 256 characters."
    } else {
        return "";
    }
}

/**
 * Validates the country part of the location form.
 * @param country The users input for the country.
 * @returns {string} An error message, or an empty string for no error.
 */
function validateCountry(country) {
    const countryRegex = new RegExp(/^[\p{L}\p{M}' -]+$/u);
    // Country is only allowed one space character
    const countryHasConsecutiveSpaces = country.includes("  ");
    if (country === undefined || country.trim() === "") {
        return "Country cannot be empty.";
    } else if (!countryRegex.test(country) || countryHasConsecutiveSpaces) {
        return "Country contains invalid characters.";
    } else if (country.trim().length > 255) {
        return "Country must be less than 256 characters."
    } else {
        return "";
    }
}

/**
 * Clears all the error messages from the location modal, used with the
 * 'Reset form' button.
 */
function clearAllErrors() {
    streetInput.classList.remove("is-invalid")
    suburbInput.classList.remove("is-invalid")
    cityInput.classList.remove("is-invalid")
    postcodeInput.classList.remove("is-invalid")
    countryInput.classList.remove("is-invalid")
    streetErrorField.style.display = 'none'
    suburbErrorField.style.display = 'none'
    cityErrorField.style.display = 'none'
    postcodeErrorField.style.display = 'none'
    countryErrorField.style.display = 'none'
}