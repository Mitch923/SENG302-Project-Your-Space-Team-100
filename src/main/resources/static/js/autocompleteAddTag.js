// Code in this file adapted from a W3 schools tutorial https://www.w3schools.com/howto/howto_js_autocomplete.asp
/**
 * Sets up autocomplete for the add tag input field
 * @param inputElement input that should display the autocomplete functionality
 */
function autocomplete(inputElement) {
    let focusedItemIndex;
    let debounceTimer;
    const DEBOUNCE_DELAY = 300;

    inputElement.addEventListener("input", function () {
        const searchText = this.value;
        closeAllAutocompleteLists();
        if (!searchText) {
            return false;
        }

        clearTimeout(debounceTimer);

        debounceTimer = setTimeout(async function () {
            focusedItemIndex = -1;

            const autocompleteList = document.createElement("DIV");
            autocompleteList.setAttribute("id",
                    inputElement.id + "autocomplete-list");
            autocompleteList.setAttribute("class", "autocomplete-items");
            inputElement.parentNode.appendChild(autocompleteList);

            try {
                const matchingTags = await getMatchingTags(searchText);
                for (let i = 0; i < matchingTags.length; i++) {

                    const tagListItem = document.createElement("DIV");
                    tagListItem.setAttribute("tabindex", "0");
                    tagListItem.innerHTML = "<strong>" + matchingTags[i].substr(
                                    0,
                                    searchText.length)
                            + "</strong>";
                    tagListItem.innerHTML += matchingTags[i].substr(
                            searchText.length);
                    tagListItem.innerHTML += "<input type='hidden' value='"
                            + matchingTags[i] + "'>";

                    tagListItem.addEventListener("click", function () {
                        inputElement.value = this.getElementsByTagName(
                                "input")[0].value;
                        inputElement.form.submit();
                        inputElement.value = "";
                        closeAllAutocompleteLists();
                    });

                    tagListItem.addEventListener("keydown", function (event) {
                        if (event.key === "Enter") {
                            event.preventDefault();
                            this.click();
                        }
                    });

                    tagListItem.addEventListener("focus", function () {
                        const allItems = autocompleteList.getElementsByTagName(
                                "div");
                        for (let j = 0; j < allItems.length; j++) {
                            allItems[j].classList.remove("autocomplete-active");
                        }
                        this.classList.add("autocomplete-active");

                        const allItemsArray = Array.from(allItems);
                        focusedItemIndex = allItemsArray.indexOf(this);
                    });

                    autocompleteList.appendChild(tagListItem);
                }

                if (matchingTags.length === 0) {
                    const noResultsItem = document.createElement("DIV");
                    noResultsItem.innerHTML += "No matching tags";
                    autocompleteList.appendChild(noResultsItem);
                }
            } catch (error) {
                console.error("Failed to fetch tags:", error);
            }
        }, DEBOUNCE_DELAY);
    });

    /**
     * Checks which key was pressed and either changes the focused option from the autocomplete list(up & down arrows)
     * or triggers a click event(enter)
     */
    inputElement.addEventListener("keydown", function (event) {
        let autocompleteItems = document.getElementById(
                this.id + "autocomplete-list");
        if (autocompleteItems) {
            autocompleteItems = autocompleteItems.getElementsByTagName("div");
        }

        if (event.keyCode === 40) { // Down Arrow
            event.preventDefault();
            focusedItemIndex++;
            setActiveItem(autocompleteItems);
        } else if (event.keyCode === 38) { // Up Arrow
            event.preventDefault();
            focusedItemIndex--;
            setActiveItem(autocompleteItems);
        } else if (event.keyCode === 13) { // Enter
            event.preventDefault();
            if (focusedItemIndex > -1 && autocompleteItems) {
                autocompleteItems[focusedItemIndex].click();
            }
        } else if (event.keyCode === 9) { // Tab
            if (focusedItemIndex === -1 && autocompleteItems
                    && autocompleteItems.length > 0) {
                event.preventDefault();
                focusedItemIndex = 0;
                autocompleteItems[focusedItemIndex].focus();
            }
        }
    });

    /**
     * Adds the styling that shows an option from the autocomplete as highlighted or active
     * @param itemsList The option from the autocomplete list that is focused
     */
    function setActiveItem(itemsList) {
        if (!itemsList) {
            return false;
        }
        removeActiveHighlight(itemsList);
        if (focusedItemIndex >= itemsList.length) {
            focusedItemIndex = 0;
        }
        if (focusedItemIndex < 0) {
            focusedItemIndex = itemsList.length - 1;
        }
        itemsList[focusedItemIndex].classList.add("autocomplete-active");
    }

    /**
     * Removes the styling that shows an option from the autocomplete as highlighted
     * @param itemsList The option from the autocomplete list that has just been unfocused
     */
    function removeActiveHighlight(itemsList) {
        for (let i = 0; i < itemsList.length; i++) {
            itemsList[i].classList.remove("autocomplete-active");
        }
    }

    /**
     * Closes all autocomplete lists in the document, except the one passed as an argument
     * @param elementToKeep The autocomplete list that shouldn't be closed
     */
    function closeAllAutocompleteLists(elementToKeep) {
        const autocompleteLists = document.getElementsByClassName(
                "autocomplete-items");
        for (let i = 0; i < autocompleteLists.length; i++) {
            if (elementToKeep !== autocompleteLists[i] && elementToKeep
                    !== inputElement) {
                autocompleteLists[i].parentNode.removeChild(
                        autocompleteLists[i]);
            }
        }
    }

    document.addEventListener("click", function (event) {
        closeAllAutocompleteLists(event.target);
    });
}

/**
 * Retrieves the tags from the client side whose names start with the searchPrefix
 * @param searchPrefix The characters that will form the start of any matching tag
 * @returns {Promise<any>} a list of tag names that start with the prefix
 */
async function getMatchingTags(searchPrefix) {
    const requestUrl = `getMatchingTags?prefix=${encodeURIComponent(
            searchPrefix)}`;
    const response = await fetch(requestUrl, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'}
    });

    if (!response.ok) {
        throw new Error("Network response was not ok");
    }

    return await response.json();
}

/**
 * Adds when the page loads calls autocomplete to set the add tag input for autocompletion
 */
document.addEventListener("DOMContentLoaded", function () {
    const tagInput = document.getElementById("tagName");
    if (tagInput) {
        autocomplete(tagInput);
    }
});