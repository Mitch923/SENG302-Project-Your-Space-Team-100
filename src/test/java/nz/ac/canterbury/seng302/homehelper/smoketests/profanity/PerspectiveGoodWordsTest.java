package nz.ac.canterbury.seng302.homehelper.smoketests.profanity;

import static org.junit.jupiter.api.Assertions.assertFalse;

import nz.ac.canterbury.seng302.homehelper.utils.profanity.ProfanityChecker;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// NOTE: this test is excluded from running in build.gradle, due to its expensive and time-consuming nature i.e. for experiments only

@SpringBootTest
public class PerspectiveGoodWordsTest {

    @Autowired
    ProfanityChecker profanityChecker;

    /**
     * !!! EXPENSIVE TEST !!! This will take a while because the time between api calls is 1 second
     *
     * @param input the input string to be tested against the profanity filter
     * @throws InterruptedException if the execution of the test is interrupted
     */
    @ParameterizedTest
    @CsvFileSource(resources = "/cleanTags.csv", numLinesToSkip = 1)
    public void testCleanTagsPerspectiveApi(String input) throws InterruptedException {
        Thread.sleep(1000); // to limit API Queries Per Second
        assertFalse(profanityChecker.isProfanePerspective(input));
    }

}
