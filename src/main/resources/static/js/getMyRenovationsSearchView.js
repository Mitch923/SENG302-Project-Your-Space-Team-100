import {
    applyPaginationCustomPageNum,
    getNumberOfItemsPerPage,
    validateAjaxReturn,
    validatePageNumber,
} from "./pagination.js";

/**
 * Fetches the html for the chosen page of renovations with the given query
 *
 * @param currentPage the page of renovations
 * @param resultsPerPage the num of renovations per page
 * @param query the input query of the user
 * @returns the html of paginated renovations
 */
function getRenovations(currentPage, resultsPerPage, query) {
    return fetch(
            `getMyRenovationsSearch?page=${currentPage}&resultsPerPage=${resultsPerPage}&query=${query}`)
    .then(response => response.text());
}

/**
 * Fetches the html for the chosen page of renovations
 *
 * @param currentPage the page of renovations
 * @param count the num of renovations per page
 * @returns the html of paginated renovations
 */
function getRenovationsPagination(currentPage, count) {
    return fetch(
            `getMyRenovationsSearch?page=${currentPage}&resultsPerPage=${count}`)
    .then(response => response.text());
}

/**
 * Set the renovations container with the html returned by the getrenovations
 *
 * @param currentPage the page of renovations
 * @param count the num of renovation per page
 */
async function applyRenovations(currentPage, count) {
    const data = await getRenovationsPagination(currentPage, count);
    const validData = validateAjaxReturn(data);
    if (validData) {
        document.getElementById('renovationsContainer').innerHTML = data;
    } else {
        window.location.reload();
    }
}

window.applyRenovations = applyRenovations;

/**
 * Calculates the number of renovations to request from the server and requests
 * the first page of renovations to show on the screen.
 *
 * @param query the input query of the user
 * @param page the page of renovation to display
 */
async function applyInitialRenovations(query, page) {
    const count = getNumberOfItemsPerPage(100, 2);
    const data = await getRenovations(page, count, query);
    const validData = validateAjaxReturn(data);
    if (validData) {
        document.getElementById('renovationsContainer').innerHTML = data;
    } else {
        window.location.reload();
    }
}

window.applyInitialRenovations = applyInitialRenovations;

window.applyPaginationCustomPageNum = applyPaginationCustomPageNum;

window.validatePageNumber = validatePageNumber;