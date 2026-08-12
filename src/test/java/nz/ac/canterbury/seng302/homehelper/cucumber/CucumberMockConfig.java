package nz.ac.canterbury.seng302.homehelper.cucumber;

import nz.ac.canterbury.seng302.homehelper.service.EmailVerificationService;
import nz.ac.canterbury.seng302.homehelper.service.SpringEmailService;
import nz.ac.canterbury.seng302.homehelper.utils.profanity.ProfanityChecker;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;

/**
 * Config class that is used to define beans that should be mocked in steps defs so they can be
 * loaded in the spring context configuration.
 */
@TestConfiguration
public class CucumberMockConfig {

    @MockBean
    private SpringEmailService springEmailService;

    @SpyBean
    private EmailVerificationService emailVerificationService;

    @Bean
    public ProfanityChecker profanityChecker() {
        return Mockito.mock(ProfanityChecker.class);
    }
}
