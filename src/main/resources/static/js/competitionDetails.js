sortByDropdown = document.getElementById('sortByDropdown');

// On page load sets the dropdown value to match the one in the url
const params = new URLSearchParams(window.location.search);
const sortByParam = params.get('sortBy');
if (sortByParam) {
    sortByDropdown.value = sortByParam;
}

/**
 * Send a request with the currently selected sortby from the dropdown,
 * and then place the resulting fragment back into the html.
 */
function getSortedResults() {
    const params = new URLSearchParams(window.location.search);
    const baseContentURL = document.getElementById(
            "paginationContent").dataset.url;

    params.set('sortBy', sortByDropdown.value);
    history.replaceState({}, '',
            `${window.location.pathname}?${params.toString()}`);
    fetch(`${baseContentURL}?pageNum=0&pageSize=8&sortBy=${sortByDropdown.value}`).then(
            res => res.text()).then((text) => {
        document.getElementById('paginationContent').innerHTML = text;
    }).catch(
            err => console.error("Error: ", err));
}

sortByDropdown.addEventListener('change', (e) => {
    e.preventDefault();
    getSortedResults();
});