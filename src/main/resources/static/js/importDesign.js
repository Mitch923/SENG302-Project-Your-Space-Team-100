const searchForm = document.getElementById("search-form");
const resultsDiv = document.getElementById("paginationContent");
const searchInput = document.getElementById("searchInput")
const filterInput = document.getElementById("filterInput");
const filterContainer = document.getElementById("filterContainer");
const autoCompleteList = document.getElementById("autocompleteList");

/**
 * Debounce utility function written by ChatGPT
 * @param {Function} func - function to run after debounce delay
 * @param {number} delay - time in ms to wait after the last call
 * @returns {Function} debounced function
 */
function debounce(func, delay = 300) {
    let timeoutId;
    return (...args) => {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => {
            func.apply(null, args);
        }, delay);
    };
}

/**
 * @typedef {Object} AutoCompleteOption
 * @property {string} name
 * @property {number} id
 */

/**
 * Takes a list of autocomplete options and shows them in the autocomplete dropdown.
 * Pass an empty list to hide the autocomplete list.
 * @param options {AutoCompleteOption[]}
 */
function showAutocompleteList(options) {
    // remove any previous autocomplete options
    autoCompleteList.innerHTML = "";
    // if there were no options specified, hide the autocomplete menu and return
    if (options.length === 0) {
        const noResultsElem = document.createElement("div");
        noResultsElem.innerText = "No renovation records match your input";
        noResultsElem.classList.add('no-interaction');
        autoCompleteList.appendChild(noResultsElem);
    } else {
        // if there are options, then show the menu and add them to the list for the user to select from
        for (let option of options) {
            // create an option element to add into the autocomplete element holding all the autocomplete options
            const optionElem = document.createElement("div");
            optionElem.classList.add("autocomplete-option");
            optionElem.setAttribute('tabindex', '0');
            optionElem.innerText = option.name;
            optionElem.dataset.renovationId = option.id.toString();
            autoCompleteList.appendChild(optionElem);
        }
    }
    autoCompleteList.hidden = false;
}

/**
 * Close the autocomplete list.
 */
function hideAutocompleteList() {
    autoCompleteList.innerHTML = "";
    autoCompleteList.hidden = true;
}

/**
 * Set to hold the currently selected filters, in the form of renovationIds.
 * @type {Set<number>}
 */
let renovationIdsToFilter = new Set();

/**
 * Add event listener for clicks in the filter bar.
 * If a close button was clicked, remove the corresponding renovation ID from the search filter list,
 * then remove the filter pill element.
 */
filterContainer.addEventListener('click', (event) => {
    if (event.target.classList.contains('btn-close')) {
        // call removeFilter with the id of the renovation recorded in the dataset of the filter pill
        removeFilter(
                parseInt(event.target.parentElement.dataset.renovationId, 10));
        event.target.parentElement.remove();
        updateAutocompleteDropdown(filterInput.value.trim())
        submitSearch();
    }
})

function handleAutoCompleteListClick(event) {
    if (event.target.classList.contains('autocomplete-option')) {
        // add the filter from the autocomplete using the inner text as the name,
        // and the dataset attribute data-renovation-id as the renovation id
        addFilter(parseInt(event.target.dataset.renovationId, 10),
                event.target.innerText);
        submitSearch();
    }
}

autoCompleteList.addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
        handleAutoCompleteListClick(event);
    }
})

autoCompleteList.addEventListener('click', handleAutoCompleteListClick);

/**
 * Removes a filter from the filter set
 * @param renovationId {number} the renovation ID you want to remove from filters that will be searched
 */
function removeFilter(renovationId) {
    console.log(`Removing filter renovation id: ${renovationId}`);
    renovationIdsToFilter.delete(renovationId);

    const params = new URLSearchParams(window.location.search);
    params.set('searchQuery', searchInput.value);
    params.set('renovations', Array.from(renovationIdsToFilter).join(","));
    history.replaceState({}, '',
            `${window.location.pathname}?${params.toString()}`);
}

/**
 * Adds a filter to the filter set, and adds an active filter element in the form of a bootstrap pill badge to the filter input.
 * @param renovationId {number} the renovation ID you want to add to filter by
 * @param name {string} the name of the filter, i.e. the name of the renovation
 */
function addFilter(renovationId, name) {
    console.log(`Adding filter renovation id: ${renovationId}`);
    // prevent any negative/zero values from causing a filter to be added
    if (renovationId < 1) {
        console.log(`Reno Id is < 1, doing nothing.`);
        hideAutocompleteList();
        return;
    }
    renovationIdsToFilter.add(renovationId);

    // set the browser url to reflect the filter params
    const params = new URLSearchParams(window.location.search);
    params.set('searchQuery', searchInput.value);
    params.set('renovations', Array.from(renovationIdsToFilter).join(","));
    history.replaceState({}, '',
            `${window.location.pathname}?${params.toString()}`);

    filterContainer.insertBefore(createFilterPillElem(renovationId, name),
            filterInput);

    hideAutocompleteList();
    // clear the filter input
    filterInput.value = "";
}

/**
 * Creates a filter pill html element used in the frontend to display an active filter.
 * @param renovationId the id of the renovation the filter corresponds to
 * @param name the name of the filter i.e. the renovation name
 * @returns {HTMLSpanElement} the pill element that will be added to the filter element html
 */
function createFilterPillElem(renovationId, name) {
    const filterPillElem = document.createElement("span");
    filterPillElem.classList.add("badge", "bg-success", "d-inline-flex",
            "align-items-center", "p-1", "mw-100")
    filterPillElem.dataset.renovationId = renovationId.toString();

    const textElem = document.createElement("span");
    textElem.innerText = name;
    textElem.classList.add("text-truncate");

    const buttonElem = document.createElement("button");
    buttonElem.ariaLabel = "Remove tag";
    buttonElem.classList.add("btn-close", "btn-close-white");
    buttonElem.style.fontSize = "0.6rem";
    buttonElem.type = "submit";

    filterPillElem.appendChild(textElem);
    filterPillElem.appendChild(buttonElem);

    return filterPillElem;
}

/**
 * Updates the autocomplete dropdown by querying the server for potential
 * name matches, and then adding these along with their renovationIds
 * to the autocmplete options
 * @param filterInputText {string} the text to use to find matching reno records with that name
 */
async function updateAutocompleteDropdown(filterInputText) {
    if (filterInputText === "") {
        hideAutocompleteList();
    } else {
        // query the server for matching renovations that the user owns with that substring as the name
        const results = await getAutocompleteResults();
        showAutocompleteList(results);
    }
}

/**
 * Event listener for the filter input, that sends a debounced call to updateAutocompleteDropdown
 * The debouncing is so we don't send an api request on every single keystroke, wait till the user has stopped typing.
 */
filterInput.addEventListener('input', debounce((event) => {
    updateAutocompleteDropdown(event.target.value.trim());
}, 300))

/**
 * Send a request with the user's search query to retrieve the renovation
 * designs in the form of a html fragment.
 */
function submitSearch() {
    const params = new URLSearchParams(window.location.search);
    params.set('searchQuery', searchInput.value);
    history.replaceState({}, '',
            `${window.location.pathname}?${params.toString()}`);
    fetch(`importDesign/search/results/paged?pageNum=0&pageSize=8&searchQuery=${encodeURIComponent(
            searchInput.value.trim())}&renovations=${Array.from(
            renovationIdsToFilter).join(",")}`).then(
            res => res.text()).then(
            html => resultsDiv.innerHTML = html).catch(
            err => console.error("Error: ", err));
}

/**
 * Get the list of autocomplete results from back end.
 * @return Promise<[]> List of AutoCompleteOptions
 */
function getAutocompleteResults() {
    const url = 'importDesign/renovation/autocomplete?query='
            + filterInput.value.trim();
    return fetch(url)
    .then(res => res.json())
    .then(json => {
        return json.filter(record => !renovationIdsToFilter.has(record.id));
    })
    .catch(
            err => {
                console.error(err);
                return [];
            });
}

searchForm.addEventListener('submit', (e) => {
    e.preventDefault();
    submitSearch();
});

submitSearch();

window.updateImportedDesignId = (element) => {
    const id = element.getAttribute("data-id");
    const name = element.getAttribute("data-name");
    const theme = document.getElementById('competitionTheme');
    document.getElementById('importDesignForm').action = `importDesign/${id}`;
    document.getElementById(
            'importConfirmationMessage').innerText = `Are you sure you want to import '${name}' into ${theme.value}`
}