/**
 * This piece of code moves the search bar modal to the level of the <body> so that it renders properly.
 * Otherwise, you run into a problem where the modal is not able to be closed because the background covers
 * the entire modal.
 */
const searchModal = document.getElementById('searchModal');
searchModal.remove();
document.body.prepend(searchModal);

document.getElementById("desktop-search").addEventListener("submit", (e) => {
    const input = document.getElementById("desktopSearchInput");
    if (input.value === "") {
        e.preventDefault();
    }
})

document.getElementById("searchFormModal").addEventListener("submit", (e) => {
    const input = document.getElementById("searchInputModal");
    if (input.value === "") {
        e.preventDefault();
    }
})