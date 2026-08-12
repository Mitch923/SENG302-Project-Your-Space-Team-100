/**
 * Takes a page button and requests the resource at that page index from the server,
 * then presents this in the 'paginatedContent' div.
 * Executed on button click
 * @param element <a> tag that is a page button
 * @returns {Promise<void>}
 */
async function goToPageFromData(element) {
    const url = element.dataset.url;
    const pageNum = parseInt(element.dataset.page, 10);
    const params = new URLSearchParams(window.location.search);
    const pageSize = params.get("pageSize") || 8; // Get from URL or default to 8

    // Build URL with pageNum and pageSize
    let fullURL = `${url}?pageNum=${pageNum}&pageSize=${pageSize}`;

    // Only add searchQuery if it exists and is non-empty
    const searchQuery = params.get("searchQuery");
    if (searchQuery && searchQuery.trim() !== "") {
        fullURL += `&searchQuery=${encodeURIComponent(searchQuery)}`;
    }
    const sortByType = params.get("sortBy");
    if (sortByType && sortByType.trim() !== "") {
        fullURL += `&sortBy=${encodeURIComponent(sortByType)}`;
    }

    try {
        const renovations = params.get("renovations");
        if (renovations) {
            fullURL += "&renovations=" + encodeURIComponent(renovations);
        }

        document.getElementById(
                "paginationContent").innerHTML = await getPaginationResults(
                fullURL);
        updateUrl(pageNum);
    } catch (err) {
        console.error(err);
    }
}

/**
 * Takes a text input and requests the resource at that page index from the server,
 * then presents this in the 'paginatedContent' div.
 * Executed when page search button is clicked
 * @param element <a> tag that is a page button
 * @param totalPages the total number of pages needed to paginate results
 * @returns {Promise<void>}
 */
// this function appears to be unused, but it actually is. It's a thymeleaf quirk
// of being evaluated at runtime
async function goToPageFromSearch(element, totalPages) {
    const url = element.dataset.url;
    const params = new URLSearchParams(window.location.search);
    const intPage = parseInt(element.value.toString().slice(0, 9), 10);
    const pageSize = params.get("pageSize") || 8; // Get from URL or default to 8
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
        let fullURL = url + "?pageNum=" + (intPage - 1) + "&pageSize="
                + pageSize; //subtract one for 0 indexing

        const sortByType = params.get("sortBy");
        if (sortByType && sortByType.trim() !== "") {
            fullURL += `&sortBy=${encodeURIComponent(sortByType)}`;
        }
        errorMessage.textContent = "";

        const searchQuery = params.get("searchQuery");
        if (searchQuery) {
            fullURL += "&searchQuery=" + encodeURIComponent(searchQuery);
        }

        const renovations = params.get("renovations");
        if (renovations) {
            fullURL += "&renovations=" + encodeURIComponent(renovations);
        }

        try {
            document.getElementById(
                    "paginationContent").innerHTML = await getPaginationResults(
                    fullURL);
            updateUrl(intPage - 1);
        } catch (err) {
            console.error(err);
        }

    } else {
        let pageInput = document.getElementById("pageNumber");
        let errorMessage = document.getElementById("pageNumberError");
        errorMessage.textContent = "The page number is outside the range of available pages";
        errorMessage.style.display = "inline";
        pageInput.classList.add('is-invalid');
    }

}

async function getPaginationResults(url) {
    const response = await fetch(url);
    return await response.text();
}

/**
 * Validate if the page number is an empty string or within the valid range
 * @param pageNumber entered number
 * @param totalPages total number of available pages
 * @returns {boolean} result where true is valid
 */
function validatePageNumberRange(pageNumber, totalPages) {
    const intInput = parseInt(pageNumber, 10);
    return !((intInput > totalPages || intInput <= 0) && pageNumber !== "");
}

/**
 * Prevents the user from entering any invalid characters into the page number field. Removes any
 * invalid integer characters as this function is called on the change of input
 *
 */
function validatePageNumber() {
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

/**
 * Updates the browser URL with the pageNum and pageSize query params. This means that clicking
 * back after navigating off the pagination will return you to the correct pageNumber
 * @param pageNum current pageNum of request
 */
function updateUrl(pageNum) {
    const params = new URLSearchParams(window.location.search);
    params.set('pageNum', pageNum);
    // Keep the existing pageSize in the URL - don't modify it
    history.replaceState({}, '',
            `${window.location.pathname}?${params.toString()}`);
}

/**
 * Initialises the pagination by calling the specific endpoint on page load.
 * Determines pageSize based on screen width or uses optional overrides.
 * Once set, pageSize is maintained in the URL for subsequent requests.
 * @param baseContentURL
 * @param pageSizeSm pageSize for small screens (<768px)
 * @param pageSizeMd pageSize for medium screens (>=768px and <1200px)
 * @param pageSizeLg pageSize for large screens (>=1200px)
 * @returns {Promise<void>}
 */
async function initPagination(baseContentURL, pageSizeSm = 4, pageSizeMd = 8,
        pageSizeLg = 8) {
    const params = new URLSearchParams(window.location.search);

    let pageNum = parseInt(params.get("pageNum"), 10);
    let pageSize = parseInt(params.get("pageSize"), 10);

    // Validate and default pageNum
    if (isNaN(pageNum) || pageNum < 0) {
        pageNum = 0;
    }

    // Determine pageSize based on screen width if not already set
    if (isNaN(pageSize) || pageSize <= 0) {
        const width = window.innerWidth;
        if (width < 768) {
            pageSize = pageSizeSm;
        } else if (width < 1200) {
            pageSize = pageSizeMd;
        } else {
            pageSize = pageSizeLg;
        }

        // Update pageSize in URL so other functions can read it
        params.set('pageSize', pageSize);
        history.replaceState({}, '',
                `${window.location.pathname}?${params.toString()}`);
    }

    let fullURL = `${baseContentURL}?pageNum=${pageNum}&pageSize=${pageSize}`;
    let sort = params.get('sortBy');
    if (sort) {
        fullURL = fullURL + '&sortBy=' + sort
    }

    try {
        document.getElementById("paginationContent").innerHTML =
                await getPaginationResults(fullURL);
    } catch (err) {
        console.error("Error loading pagination results:", err);
    }
}

module.exports = {
    validatePageNumberRange,
    initPagination,
    validatePageNumber,
    goToPageFromSearch,
    goToPageFromData
};

window.initPagination = initPagination;
window.validatePageNumber = validatePageNumber;
window.goToPageFromSearch = goToPageFromSearch;
window.goToPageFromData = goToPageFromData;