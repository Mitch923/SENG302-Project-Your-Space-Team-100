import {
    validateAjaxReturn,
    validatePageNumber,
    validatePageNumberRange,
} from "./pagination.js";

/**
 * Fetchs the html for the chosen page of design for a given renovation record
 *
 * @param id renovation id
 * @param currentPage the page of designs
 * @param resultsPerPage the num of designs per page
 * @returns the html of the designs
 */

function getDesigns(id, currentPage, resultsPerPage) {
    return fetch(
            `viewRenovation/${id}/getDesigns?page=${currentPage}&resultsPerPage=${resultsPerPage}`)
    .then(response => response.text());
}

/**
 * Set the design container with the html returned by the getDesigns
 *
 * @param id renovation id
 * @param currentPage the page of designs
 * @param designsPerPage the num of design per page
 */

async function applyDesigns(id, currentPage, designsPerPage) {
    const data = await getDesigns(id, currentPage, designsPerPage);
    const validData = validateAjaxReturn(data);
    if (validData) {
        document.getElementById('designContainer').innerHTML = data;
        await initialiseDropdowns();
    } else {
        window.location.reload();
    }
}

window.applyDesigns = applyDesigns;

/**
 * Sets the renovation container if the pageNumber for the pageNumber input field is a valid number
 *
 * @param id the renovation id of design
 * @param totalPages the total number of pages
 * @param designsPerPage the num of renovation per page
 */
export async function applyDesignsCustomPageNum(id, totalPages,
        designsPerPage) { // Beware intellij doesn't identify usages in html!!
    const page = document.getElementById('pageNumber').value
    const intPage = parseInt(page.toString().slice(0, 6), 10);
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
        await applyDesigns(id, intPage, designsPerPage);
    } else {
        let pageInput = document.getElementById("pageNumber");
        let errorMessage = document.getElementById("pageNumberError");
        errorMessage.textContent = "The page number is outside the range of available pages";
        errorMessage.style.display = "inline";
        pageInput.classList.add('is-invalid');
    }
}

window.applyDesignsCustomPageNum = applyDesignsCustomPageNum;

/**
 * Updates a selected design's icon name
 *
 * @param designId the selected design
 * @param csrfHeader csrf header for validation
 * @param csrfToken csrf token for validation
 * @param selectedIcon the selected icon's name
 */

function postNewIcon(designId, csrfHeader, csrfToken, selectedIcon) {
    fetch(`viewRenovation/updateIcon/${designId}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({iconName: selectedIcon})
    });
}

/**
 * Sets up event listeners for icon selector menus.
 *
 * @returns {Promise<void>}
 */

async function initialiseDropdowns() {
    // addBootstrapDropdownListeners();

    document.querySelectorAll('[data-bs-toggle="dropdown"]').forEach(
            dropdownToggle => {
                dropdownToggle.addEventListener('show.bs.dropdown',
                        function () {
                            const card = this.closest('.hover-animate');
                            if (card) {
                                card.classList.add('dropdown-open');
                            }

                            // Disable hover effect on all other cards
                            document.querySelectorAll('.hover-animate').forEach(
                                    otherCard => {
                                        if (otherCard !== card) {
                                            otherCard.classList.add('no-hover');
                                        }
                                    });
                        });

                dropdownToggle.addEventListener('hide.bs.dropdown',
                        function () {
                            const card = this.closest('.hover-animate');
                            if (card) {
                                card.classList.remove('dropdown-open');
                            }

                            // Re-enable hover effect on all cards
                            document.querySelectorAll(
                                    '.hover-animate.no-hover').forEach(
                                    otherCard => {
                                        otherCard.classList.remove('no-hover');
                                    });
                        });
            });

    document.querySelectorAll('.design-icon').forEach(item => {
        item.addEventListener('click', function (e) {
            e.preventDefault();

            // The icon clicked
            let selectedIcon = this.getAttribute('id');
            if (!selectedIcon) {
                console.error('Design icon missing ID');
                return;
            }

            // The card the selector is in
            const card = this.closest('.design-card-body');
            if (!card) {
                console.error('Card element not found');
                return;
            }

            let selectedIconDiv = card.querySelector('[id^="selectedIcon-"]');
            let removeIconButton = card.querySelector('.remove-icon-button');

            if (!selectedIconDiv) {
                console.error('Required elements missing in card');
                return;
            }

            if (selectedIcon === 'plus') { // Handle removing icon
                selectedIconDiv.innerHTML = `
          <div>
            <svg class="bi bi-plus-lg" fill="currentColor" height="16" viewBox="0 0 16 16" width="100%" xmlns="http://www.w3.org/2000/svg">
              <path d="M8 2a.5.5 0 0 1 .5.5v5h5a.5.5 0 0 1 0 1h-5v5a.5.5 0 0 1-1 0v-5h-5a.5.5 0 0 1 0-1h5v-5A.5.5 0 0 1 8 2" fill-rule="evenodd"/>
            </svg>
          </div>`;
            } else {
                selectedIconDiv.innerHTML = this.innerHTML;
            }

            // Ensure remove icon is only visible when an Icon is not selected
            if (removeIconButton) {
                if (selectedIcon === "plus") {
                    removeIconButton.classList.remove('d-flex');
                    removeIconButton.classList.add('force-hidden');
                } else {
                    removeIconButton.classList.remove('force-hidden');
                    removeIconButton.classList.add('d-flex');
                }
            }

            // The design to update
            let designId = card.querySelector('[id=designId]').value;

            // Get csrf token and header for post request validation
            const csrfToken = document.querySelector(
                    "meta[name='_csrf']").getAttribute("content");
            const csrfHeader = document.querySelector(
                    "meta[name='_csrf_header']").getAttribute("content");

            postNewIcon(designId, csrfHeader, csrfToken, selectedIcon);
        });
    });
}

window.initialiseDropdowns = initialiseDropdowns;

window.validatePageNumber = validatePageNumber;
