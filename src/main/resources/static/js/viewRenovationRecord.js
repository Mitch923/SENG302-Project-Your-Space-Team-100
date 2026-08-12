/**
 * Triggered by the on change of the checkbox this function makes a fetch request
 * to an endpoint that updates whether the renovation is public or not(visibility).
 * @param checkbox The checkbox that triggered the method call
 * @param id Id of the record to update
 */
window.toggleIsPublic = function (checkbox, id) {
    const visibility = checkbox.checked;
    const csrfToken = document.querySelector("meta[name='_csrf']").getAttribute(
            "content");
    const csrfHeader = document.querySelector(
            "meta[name='_csrf_header']").getAttribute(
            "content");
    fetch(`viewRenovation/${id}/setVisibility?visibility=${visibility}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            [csrfHeader]: csrfToken
        }
    })
    .then(response => {
    })
}