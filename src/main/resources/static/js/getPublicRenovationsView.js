import {
    applyPaginationCustomPageNum,
    getNumberOfItemsPerPage,
    validateAjaxReturn,
    validatePageNumber,
} from "./pagination.js";

/**
 * Fetchs the html for the chosen page of public renovations
 *
 * @param currentPage the page of renovations to retrieve
 * @param count the num of renovations per page
 * @returns the html of paginated renovations
 */
function getPublicRenovations(currentPage, count) {
    return fetch(
            'getPublicRenovations?page=' + currentPage + '&count=' + count)
    .then(response => response.text());
}

/**
 * Set the renovations container with the html returned by the getPublicRenovations
 *
 * @param currentPage the page of renovations to retrieve
 * @param count the num of renovations per page
 */
async function applyPublicRenovations(currentPage, count) {
    const data = await getPublicRenovations(currentPage, count);
    const validData = validateAjaxReturn(data);
    if (validData) {
        document.getElementById('publicRenovationsContainer').innerHTML = data;
    } else {
        window.location.reload();
    }
}

window.applyPublicRenovations = applyPublicRenovations;

/**
 * Calculates the number of renovations to request from the server and requests
 * the first page of renovations to show on the screen.
 */
async function applyInitialPublicRenovations(page) {
    const count = getNumberOfItemsPerPage(100, 2);
    const data = await getPublicRenovations(page, count);
    const validData = validateAjaxReturn(data);
    if (validData) {
        document.getElementById('publicRenovationsContainer').innerHTML = data;
    } else {
        window.location.reload();
    }
}

window.applyInitialPublicRenovations = applyInitialPublicRenovations;

window.applyPublicRenovationsCustomPageNum = applyPaginationCustomPageNum;

window.validatePageNumber = validatePageNumber;
