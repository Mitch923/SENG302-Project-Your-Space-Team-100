const csrfToken = document.querySelector(
        "meta[name='_csrf']").getAttribute("content");
const csrfHeader = document.querySelector(
        "meta[name='_csrf_header']").getAttribute("content");

const submitEntryButtonModal = document.getElementById(
        'confirm-submit-entry-button');
const designIdElement = document.getElementById('currentUserEntryId');
const submitEntryButtonPage = document.getElementById('submit-design-button');
const submittedMessage = document.getElementById('submitted-message');

if (submitEntryButtonModal && designIdElement) {
    submitEntryButtonModal.addEventListener('click', (e) => {
        e.preventDefault();
        const url = 'submitEntry/' + designIdElement.value;

        fetch(url, {
            method: "POST",
            headers: {
                [csrfHeader]: csrfToken,
            }
        }).then(re => {
            if (re.ok) {
                submitEntryButtonPage.remove();
                submittedMessage.classList.remove('d-none');
                showSuccessToast("Your competition entry has been submitted.");
            } else {
                showFailToast("Error submitting competition entry.")
            }
        }).catch(err => {
            showFailToast("Error submitting competition entry.")
            console.error(err);
        });
    });
}

/**
 * Shows a "success" style toast on the design edit page with the given message
 * @param message - the message to display in the toast
 */
function showSuccessToast(message) {
    const successToastElem = document.getElementById("success-message");
    const toastBody = document.querySelector('#success-message .toast-body');
    if (toastBody != null) {
        toastBody.textContent = message;
    }
    const toastBootstrap = bootstrap.Toast.getOrCreateInstance(
            successToastElem);
    toastBootstrap.show();
}

/**
 * Shows a "fail" style toast on the design edit page with the given message
 * @param message - the message to display in the toast
 */
function showFailToast(message) {
    const failToastElem = document.getElementById("fail-message");
    const toastBody = document.querySelector('#fail-message .toast-body');
    if (toastBody != null) {
        toastBody.textContent = message;
    }
    const toastBootstrap = bootstrap.Toast.getOrCreateInstance(failToastElem);
    toastBootstrap.show();
}