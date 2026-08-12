// Code inspired by ChatGPT "how do I use javascript to append query parameters to these links based on screen size"
function getCountByScreenSize() {
    if (window.innerWidth < 600) {
        return 1;
    }  // Mobile (Small Screens)
    if (window.innerWidth < 1024) {
        return 2;
    } // Tablet
    return 4; // Desktop
}

/**
 * function to set renovation links so that when a renovation is opened it paginates
 * differently depending on if the user came from a smaller screen size or not
 */
function updateRenovationLinks() {
    let designsPerPage = getCountByScreenSize();
    let links = document.querySelectorAll("a#renovation-link"); // Select all links to be modified
    links.forEach(link => {
        let baseUrl = link.getAttribute("href"); // Get existing href (e.g., "/viewRenovation/5")
        link.setAttribute("href",
                `${baseUrl}?page=1&designsPerPage=${designsPerPage}`);
    });
}

// Run this function on page load
document.addEventListener("DOMContentLoaded", updateRenovationLinks);