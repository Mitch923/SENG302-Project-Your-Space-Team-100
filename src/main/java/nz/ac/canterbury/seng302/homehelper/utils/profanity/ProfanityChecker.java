package nz.ac.canterbury.seng302.homehelper.utils.profanity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProfanityChecker {

    private final String PERSPECTIVE_ENDPOINT = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=";
    Logger logger = LoggerFactory.getLogger(ProfanityChecker.class);
    private RestTemplate restTemplate = new RestTemplate();
    @Value("${app.perspective.api.key}")
    private String API_KEY;

    public ProfanityChecker() {
    }

    // for testing with mock restTemplate
    public ProfanityChecker(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Gets a profanity score from PerspectiveAPI, then compares this to a probability threshold
     *
     * @param input string to check if it is profane
     * @return true if profanity detected, false if not
     */
    public boolean isProfanePerspective(String input) {
        // use google perspective api to check whether string is profane
        double result = analyzeProfanity(input);
        return result > 0.40; // if profanity probability is above 40%
    }

    /**
     * Sends a POST request to the perspective comment analyzer endpoint containing the specified
     * text, then returns the PROFANITY metric.
     *
     * @param text the text you want to analyze against PROFANITY
     * @return a probability between 0-1 indicating how toxic the text may be.
     */
    public double analyzeProfanity(String text) {

        logger.info("Analysing profanity for text '{}'", text);
        Comment comment = new Comment(text);

        // create Profanity value for requested attributes
        Map<String, Object> requestedAttributes = new HashMap<>();
        requestedAttributes.put("PROFANITY", new HashMap<>());
        // only use english language to analyze
        String[] languages = new String[]{"en"};

        // set doNotStore to true for full privacy of requests
        PerspectiveRequest requestBody = new PerspectiveRequest(comment, requestedAttributes,
                languages, true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PerspectiveRequest> request = new HttpEntity<>(requestBody, headers);

        // Create an ObjectMapper for JSON serialization, method of serialization recommended from chatGPT
        ObjectMapper objectMapper = new ObjectMapper();

        if (logger.isDebugEnabled()) {
            try {
                String requestJson = objectMapper.writeValueAsString(requestBody);
                logger.debug("Perspective API request JSON: {}", requestJson);
            } catch (JsonProcessingException ex) {
                logger.warn("Failed to serialize Perspective request: {}", ex.getMessage());
            }
        }

        ResponseEntity<PerspectiveResponse> response;

        try {
            response = restTemplate.postForEntity(PERSPECTIVE_ENDPOINT + API_KEY, request,
                    PerspectiveResponse.class);
            // Convert the response body to JSON and log it
            if (logger.isDebugEnabled()) {
                try {
                    String responseJson = objectMapper.writeValueAsString(response.getBody());
                    logger.debug("Perspective API response JSON: {}", responseJson);
                } catch (JsonProcessingException ex) {
                    logger.warn("Failed to serialize Perspective response: {}", ex.getMessage());
                }
            }
        } catch (HttpClientErrorException ex) {
            // 400 Bad Request specific handling
            logger.info("Endpoint: {}", PERSPECTIVE_ENDPOINT);
            logger.error("analyzeProfanity: Analyzing FAILED: HttpClientErrorException: {}",
                    ex.getResponseBodyAsString());
            return 0.0;
        }

        AttributeScore profanityScore;

        try {
            profanityScore = Objects.requireNonNull(response.getBody()).getAttributeScores()
                    .get("PROFANITY");
        } catch (NullPointerException ex) {
            logger.error(
                    "analyzeProfanity: extracting profanityScore FAILED: NullPointerException: {}",
                    ex.getMessage());
            return 0.0;
        }

        if (profanityScore != null) {
            double score = profanityScore.getSummaryScore().getValue();
            logger.info("Profanity score {}", score);
            return score;
        }

        logger.error("profanityScore is null, defaulting to 0.0");
        return 0.0;
    }
}
