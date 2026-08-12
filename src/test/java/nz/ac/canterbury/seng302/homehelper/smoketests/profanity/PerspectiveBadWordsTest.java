package nz.ac.canterbury.seng302.homehelper.smoketests.profanity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import nz.ac.canterbury.seng302.homehelper.utils.profanity.ProfanityChecker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// NOTE: this test is excluded from running in build.gradle, due to its expensive and time-consuming nature i.e. for experiments only

@SpringBootTest
public class PerspectiveBadWordsTest {

    // Counters for pass/fail
    private static int passCount = 0;
    private static int failCount = 0;
    @Autowired
    ProfanityChecker profanityChecker;

    /*
     * TEST INPUT CSV COMPILED BY GEMINI: The list combines common English profanities and their
     * obfuscated variations, including those using asterisks, numbers, and symbols.
     * It is not exhaustive. The inclusion of racial, homophobic, and other slurs is
     * for informational purposes only and does not imply endorsement or acceptance
     * of their use. Many other offensive terms exist and are intentionally omitted
     * for polite and ethical reasons.
     */

    // Log the results after all tests
    @AfterAll
    public static void logResults() {
        int totalTests = passCount + failCount;
        double passPercentage = (double) passCount / totalTests * 100;
        double failPercentage = (double) failCount / totalTests * 100;

        System.out.println("Test Summary:");
        System.out.println("Total tests run: " + totalTests);
        System.out.println("Tests passed: " + passCount + " (" + passPercentage + "%)");
        System.out.println("Tests failed: " + failCount + " (" + failPercentage + "%)");
    }

    /**
     * !!! EXPENSIVE TEST !!! This will take a while because the time between api calls is 1 second
     *
     * @param input the input string to be tested against the profanity filter
     * @throws InterruptedException if the execution of the test is interrupted
     */
    @ParameterizedTest
    @CsvFileSource(resources = "/profanities.csv", numLinesToSkip = 1)
    public void testProfaneTagsPerspectiveApi(String input) throws InterruptedException {
        Thread.sleep(1000); // to limit API Queries Per Second
        try {
            assertTrue(profanityChecker.isProfanePerspective(input));
            passCount++; // Increment pass counter if the assertion passes
        } catch (AssertionError e) {
            failCount++; // Increment fail counter if the assertion fails
        }
    }
}
