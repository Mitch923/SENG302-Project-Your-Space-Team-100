package nz.ac.canterbury.seng302.homehelper.cucumber.hooks;

import java.util.HashMap;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Helper class that allows for abstraction of cucumber step defs to be used between feature files.
 */
@Component
public class TestContext {

    public MvcResult httpResponse;

    /**
     * Helper method to extract errors from the model view.
     */
    public HashMap<String, String> getErrorsFromModel() {
        Object rawErrors = httpResponse.getModelAndView().getModel().get("errors");

        if (!(rawErrors instanceof HashMap)) {
            throw new IllegalStateException("Expected 'errors' to be a HashMap");
        }

        return (HashMap<String, String>) rawErrors;
    }
}