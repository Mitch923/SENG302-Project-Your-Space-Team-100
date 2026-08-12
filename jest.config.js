module.exports = {
    collectCoverage: true,
    collectCoverageFrom: ['src/**/*.{ts,tsx}'],
    coverageDirectory: './build/reports/coverage/jest',
    coverageReporters: ["lcov", "text"],
    testEnvironment: 'jsdom',
    preset: 'ts-jest/presets/js-with-ts',
    transformIgnorePatterns: [
        "node_modules/(?!(three)/)" // jest uses CommonJS so put any ES node_modules here
    ],
    moduleNameMapper: {
        '^@/util/csrfProvider$': '<rootDir>/src/test/jest/__mocks__/csrfprovider_mocks.ts',
        '^@/(.*)$': '<rootDir>/src/main/ts/$1',
    },
    extensionsToTreatAsEsm: [".ts"]
};