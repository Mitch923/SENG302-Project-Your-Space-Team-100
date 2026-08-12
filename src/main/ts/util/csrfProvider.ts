/**
 * Provides access to the csrf token and header. Lazily loads and throws an error if the csrf metadata doesn't exist.
 */
class CSRFProvider {
    private static csrfHeaderName: HTMLMetaElement | null = null;
    private static csrfToken: HTMLMetaElement | null = null;

    public static getCsrfHeaderName() {
        this.csrfHeaderName ??= document.querySelector('meta[name="_csrf_header"]');
        if (this.csrfHeaderName === null) {
            throw new Error("CSRF header name is missing");
        }
        return this.csrfHeaderName.content;
    }

    public static getCsrfToken() {
        this.csrfToken ??= document.querySelector('meta[name="_csrf"]');
        if (this.csrfToken === null) {
            throw new Error("CSRF token is missing");
        }
        return this.csrfToken.content;
    }
}

export default CSRFProvider;