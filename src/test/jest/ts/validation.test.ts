const nameEmptyMessage = "Design name cannot be empty";
const nameTooLongMessage = "Design name must be 255 characters or less";
const descriptionTooLongMessage = "Design description must be 512 characters or less";

// Initialise mock DOM elements
const nameErrorElement = document.createElement('p');
const descriptionErrorElement = document.createElement('p');
const nameInputElement = document.createElement('input');
const descriptionInputElement = document.createElement('textarea');
const charCounterElement = document.createElement('span');

jest.spyOn(document, 'getElementById').mockImplementation((id) => {
    if (id === "designNameError") return nameErrorElement;
    if (id === "designDescriptionError") return descriptionErrorElement;
    if (id === "designName") return nameInputElement;
    if (id === "designDescription") return descriptionInputElement;
    if (id === "charCount") return charCounterElement;
    return null;
});

// Grok Generated Mock
jest.mock('three/examples/jsm/loaders/GLTFLoader', () => ({
    GLTFLoader: jest.fn().mockImplementation(() => ({
        load: jest.fn((url, callback) => {
            callback({
                scene: {add: jest.fn(), remove: jest.fn(), children: []},
                scenes: [{add: jest.fn(), remove: jest.fn(), children: []}],
                animations: [],
            });
        }),
        loadAsync: jest.fn().mockResolvedValue({
            scene: {add: jest.fn(), remove: jest.fn(), children: []},
            scenes: [{add: jest.fn(), remove: jest.fn(), children: []}],
            animations: [],
        }),
        setPath: jest.fn().mockReturnThis(),
        setResourcePath: jest.fn().mockReturnThis(),
        setRequestHeader: jest.fn().mockReturnThis(),
    })),
}));

// Needs to be down here as the file has global code relying on the mock
const validation = require('../../../main/ts/util/validation')

const validNames = [
    "This is a valid!",
    "#emojis 👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦👨‍👩‍👧‍👦",
    "Yes, you're allowed special characters! @#$%^&*()_",
    "a".repeat(255),
    "🐫".repeat(255),
]

test.each(validNames)('validateDesignName(), valid name, returns true', (name: string) => {
    nameInputElement.value = name;

    const validName: boolean = validation.validateDesignName()

    expect(validName).toBe(true);
})

test.each(validNames)('validateDesignName(), valid name, removes error message', (name: string) => {
    nameInputElement.value = name;
    nameErrorElement.textContent = "Yo this is an error message";

    validation.validateDesignName()

    expect(nameErrorElement.innerText).toBe('');
})

test.each(validNames)('validateDesignName(), valid name, removes error class', (name: string) => {
    nameInputElement.value = name;
    nameInputElement.classList.add('is-invalid');

    validation.validateDesignName()

    expect(nameInputElement.classList.contains('is-invalid')).toBe(false);
})

const invalidNames = [
    "",
    "a".repeat(256),
    "🦧".repeat(256)
]

const invalidNamesAndErrors = [
    ["", nameEmptyMessage],
    ["a".repeat(256), nameTooLongMessage],
    ["🦧".repeat(256), nameTooLongMessage],
]

test.each(invalidNames)('validateDesignName(), invalid name, returns false', (name: string) => {
    nameInputElement.value = name;

    const validName: boolean = validation.validateDesignName()

    expect(validName).toBe(false);
})

test.each(invalidNamesAndErrors)('validateDesignName(), invalid name, adds error message', (name: string, errorMessage: string) => {
    nameInputElement.value = name;
    nameErrorElement.textContent = "";

    validation.validateDesignName()

    expect(nameErrorElement.innerText).toBe(errorMessage);
})

test.each(invalidNames)('validateDesignName(), invalid name, adds error class', (name: string) => {
    nameInputElement.value = name;
    nameInputElement.classList.remove('is-invalid');

    validation.validateDesignName()

    expect(nameInputElement.classList.contains('is-invalid')).toBe(true);
})