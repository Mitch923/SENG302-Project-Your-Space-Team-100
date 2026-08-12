class MockModal {
    public static getOrCreateInstance = jest.fn().mockReturnValue({
        show: jest.fn(),
        hide: jest.fn(),
        toggle: jest.fn(),
        dispose: jest.fn(),
    });

    public show = jest.fn();
    public hide = jest.fn();
    public toggle = jest.fn();
    public dispose = jest.fn();

    constructor(element: any, options?: any) {
    }
}

export {MockModal as Modal};