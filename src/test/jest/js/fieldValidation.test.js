const fieldValidation = require(
        '../../../main/resources/static/js/fieldValidation');

test.each([
    ['👨‍👩‍👧‍👦', 1],
    ['abcdefg', 7],
])('countGraphemeClusters(str) returns correct count', (str, expectedCount) => {
    expect(fieldValidation.countGraphemeClusters(str)).toEqual(expectedCount);
})