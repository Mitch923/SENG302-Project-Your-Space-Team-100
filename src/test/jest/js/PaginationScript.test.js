const paginationScript
        = require(
        '../../../main/resources/static/js/paginationScript.js');

describe('validatePageNumberRange', () => {
    describe('Valid cases', () => {
        test('return true for empty string', () => {
            expect(paginationScript.validatePageNumberRange("", 10)).toBe(true);
        });

        test('return true for valid page numbers within range', () => {
            expect(paginationScript.validatePageNumberRange("1", 38)).toBe(
                    true);
            expect(paginationScript.validatePageNumberRange("5", 10)).toBe(
                    true);
            expect(paginationScript.validatePageNumberRange("10", 10)).toBe(
                    true);
        });

        test('return true for valid page numbers as numbers', () => {
            expect(paginationScript.validatePageNumberRange(1, 10)).toBe(true);
            expect(paginationScript.validatePageNumberRange(5, 10)).toBe(true);
            expect(paginationScript.validatePageNumberRange(10, 10)).toBe(true);
        });
        test('should handle decimal numbers although decimals are not allowed to be typed',
                () => {
                    expect(paginationScript.validatePageNumberRange("1.5",
                            10)).toBe(
                            true);
                    expect(paginationScript.validatePageNumberRange("10.9",
                            10)).toBe(
                            true);
                    expect(paginationScript.validatePageNumberRange("11.1",
                            10)).toBe(
                            false);
                });
    });

    describe('Invalid cases', () => {
        test('return false for page numbers greater than totalPages',
                () => {
                    expect(paginationScript.validatePageNumberRange("11",
                            10)).toBe(false);
                    expect(paginationScript.validatePageNumberRange("100",
                            10)).toBe(false);
                    expect(paginationScript.validatePageNumberRange(15,
                            10)).toBe(false);
                });

        test('return false for page numbers less than or equal to 0',
                () => {
                    expect(paginationScript.validatePageNumberRange("0",
                            10)).toBe(false);
                    expect(paginationScript.validatePageNumberRange("-1",
                            10)).toBe(false);
                    expect(paginationScript.validatePageNumberRange("-5",
                            10)).toBe(false);
                    expect(paginationScript.validatePageNumberRange(0,
                            10)).toBe(false);
                });
    });

});