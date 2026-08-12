/**
 * Shows a "success" style toast on the design edit page with the given message
 * @param message - the message to display in the toast
 */
export function showSuccessToast(message: string): void {
    const successToastElem = document.getElementById("success-message");
    const toastBody = document.querySelector('#success-message .toast-body');
    if (toastBody != null) {
        toastBody.textContent = message;
    }
    // @ts-ignore: Assume bootstrap.Toast is globally available
    const toastBootstrap = bootstrap.Toast.getOrCreateInstance(successToastElem);
    toastBootstrap.show();
}

/**
 * Shows a "fail" style toast on the design edit page with the given message
 * @param message - the message to display in the toast
 */
export function showFailToast(message: string): void {
    const failToastElem = document.getElementById("fail-message");
    const toastBody = document.querySelector('#fail-message .toast-body');
    if (toastBody != null) {
        toastBody.textContent = message;
    }
    // @ts-ignore: Assume bootstrap.Toast is globally available
    const toastBootstrap = bootstrap.Toast.getOrCreateInstance(failToastElem);
    toastBootstrap.show();
}

/**
 * Prematurely remove the fail toast notification. This is used when object scale validation
 * succeeds so the toast isn't still there.
 */
export function removeFailToast(): void {
    const failToastElem = document.getElementById("fail-message");
    // @ts-ignore: Assume bootstrap.Toast is globally available
    const toastBootstrap = bootstrap.Toast.getOrCreateInstance(failToastElem);
    toastBootstrap.hide();
}