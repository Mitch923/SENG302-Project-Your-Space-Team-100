class CSRFProvider {
    public static getCsrfHeaderName = jest.fn().mockReturnValue("MockCSRFHeaderName");
    public static getCsrfToken = jest.fn().mockReturnValue("MockCSRFToken");
}

export default CSRFProvider;