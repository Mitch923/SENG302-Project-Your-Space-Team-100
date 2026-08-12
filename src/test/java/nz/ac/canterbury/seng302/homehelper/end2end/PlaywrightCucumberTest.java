package nz.ac.canterbury.seng302.homehelper.end2end;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.junit.platform.engine.Constants;
import io.cucumber.spring.CucumberContextConfiguration;
import nz.ac.canterbury.seng302.homehelper.HomeHelperApplication;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.ConfigurationParameters;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("end2endFeatures")
@ConfigurationParameters({
        @ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "nz.ac.canterbury.seng302.homehelper.end2end"),
        @ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-report/cucumber.html"),
        @ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
})
@ContextConfiguration(classes = HomeHelperApplication.class)
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("end2end")
public class PlaywrightCucumberTest {

    static Playwright playwright;
    static Browser browser;
    static BrowserContext browserContext;
    static Page page;
    static String baseUrl;
    @LocalServerPort
    private int port;

    @BeforeAll
    public static void openResources() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
        // The current config runs the tests headlessly - this means that the browser is not displayed.
        // To run the browser headed (you can see the browser GUI as the tests run), switch to the following line:
        // browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

    }

    @AfterAll
    public static void closeResources() {
        playwright.close();
    }

    @Before
    public void openContext() {
        baseUrl = "http://localhost:" + port;
        browserContext = browser.newContext();
        page = browserContext.newPage();
        page.navigate(baseUrl);
    }

    @After
    public void closeContext() {
        browserContext.close();
    }

}
