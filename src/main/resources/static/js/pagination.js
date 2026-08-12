/**
 * Returns the number of items to display on the page during a paginated
 * request, taking into account the number of rows of items, the height of the
 * item, and the offset to account for other elements on the page.
 *
 * @param itemHeight The height of the item in px
 * @param offset The number of rows of items to offset by
 * @returns {number} number of items to request
 */
export function getNumberOfItemsPerPage(itemHeight, offset) {
    const windowHeight = window.innerHeight
    let numRows = Math.floor(windowHeight / itemHeight) - offset;
    numRows = numRows < 3 ? 3 : numRows;
    if (window.matchMedia('(min-width: 768px').matches) { // md & larger - has two columns
        return numRows * 2;
    } else { // sm & xs - has one column
        return numRows;
    }
}

/**
 * Validates that the given data returned from an ajax call isn't the Login page
 * This is needed as if the user's session expires/becomes no longer valid while
 * they're on a page the return of the request will be the login page which will
 * be embedded into the screen if not validated
 *
 * @param data the return data of the ajax call
 * @return boolean whether the data is valid or not
 */
export function validateAjaxReturn(data) {
    const loginPage = "<!DOCTYPE html>\n"
            + "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/html\">\n"
            + "<head>\n"
            + "  <head>\n"
            + "    <meta charset=\"UTF-8\">\n"
            + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
            + "\n"
            + "    <!-- Dynamic Page Title -->\n"
            + "    <title>Login | Your Space</title>"

    if (data.startsWith(loginPage)) {
        return false;
    } else {
        return true;
    }
}

/**
 * Sets the renovation container if the pageNumber for the pageNumber input field is a valid number
 *
 * @param totalPages the total number of pages
 * @param itemsPerPage the num of items per page
 * @param applicationMethod the method for how where to source the renovations that should be applied. e.g applyPublicRenovations
 */
export async function applyPaginationCustomPageNum(totalPages, itemsPerPage,
        applicationMethod) { // Beware intellij doesn't identify usages in html!!
    const page = document.getElementById('pageNumber').value
    const intPage = parseInt(page.toString().slice(0, 9), 10);
    let pageInput = document.getElementById("pageNumber");
    let errorMessage = document.getElementById("pageNumberError");

    if (isNaN(intPage)) {
        errorMessage.textContent = "Page entered is not a number";
        errorMessage.style.display = "inline";
        pageInput.classList.add('is-invalid');
    } else if (validatePageNumberRange(intPage, totalPages)) {
        errorMessage.style.display = "none";
        pageInput.classList.remove('is-invalid');
        errorMessage.textContent = ""
        await applicationMethod(intPage, itemsPerPage);
    } else {
        let pageInput = document.getElementById("pageNumber");
        let errorMessage = document.getElementById("pageNumberError");
        errorMessage.textContent = "The page number is outside the range of available pages";
        errorMessage.style.display = "inline";
        pageInput.classList.add('is-invalid');
    }
}

/**
 * Validate if the page number is an empty string or within the valid range
 * @param pageNumber entered number
 * @param totalPages total number of available pages
 * @returns {boolean} result where true is valid
 */
export function validatePageNumberRange(pageNumber, totalPages) {
    const intInput = parseInt(pageNumber, 10);
    return !((intInput > totalPages || intInput <= 0) && pageNumber !== "");
}

/**
 * Prevents the user from entering any invalid characters into the page number field. Removes any
 * invalid integer characters as this function is called on the change of input
 *
 */
export function validatePageNumber() {
    const pageInput = document.getElementById("pageNumber");

    // ChatGPT generated code start - Its purpose is to prevent the cursor from jumping when the input value is edited
    const start = pageInput.selectionStart;
    const end = pageInput.selectionEnd;

    const oldValue = pageInput.value;
    // Replace everything except digits and minus sign (ensure minus is only at the beginning)
    let newValue = oldValue.replace(/[^0-9\-]/g, '');

    newValue = newValue.replace(/-/g, '');

    if (oldValue.charAt(0) === '-' && newValue !== oldValue) {
        newValue = '-' + newValue;  // Add minus back at the front if it was originally there
    }

    pageInput.value = newValue;
    // Adjust cursor position if characters were removed before the cursor
    const diff = oldValue.length - newValue.length;
    pageInput.setSelectionRange(start - diff, end - diff);
    // ChatGPT generated code end
}