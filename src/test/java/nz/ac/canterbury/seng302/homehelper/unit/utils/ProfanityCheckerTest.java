package nz.ac.canterbury.seng302.homehelper.unit.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nz.ac.canterbury.seng302.homehelper.utils.profanity.AttributeScore;
import nz.ac.canterbury.seng302.homehelper.utils.profanity.Comment;
import nz.ac.canterbury.seng302.homehelper.utils.profanity.PerspectiveRequest;
import nz.ac.canterbury.seng302.homehelper.utils.profanity.PerspectiveResponse;
import nz.ac.canterbury.seng302.homehelper.utils.profanity.ProfanityChecker;
import nz.ac.canterbury.seng302.homehelper.utils.profanity.Score;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class ProfanityCheckerTest {

    private final String profaneString = "shit";
    private final String cleanString = "hello";
    private final HttpEntity<PerspectiveRequest> profaneRequest = createRequest(profaneString);
    private final HttpEntity<PerspectiveRequest> cleanRequest = createRequest(cleanString);
    private final String API_KEY = "fakeApiKey";
    private final String PERSPECTIVE_ENDPOINT =
            "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=" + API_KEY;
    @Mock
    private RestTemplate restTemplate;

    private ProfanityChecker profanityChecker;

    @BeforeEach
    public void init() {
        profanityChecker = new ProfanityChecker(restTemplate);
    }

    /**
     * creates a mock request object that contains the inString passed in
     */
    private HttpEntity<PerspectiveRequest> createRequest(String inString) {
        Comment comment = new Comment(inString);
        // create requestedAttributes with the PROFANITY attribute inside
        Map<String, Object> requestedAttributes = new HashMap<>();
        requestedAttributes.put("PROFANITY", new HashMap<>());
        // set language to english
        String[] languages = new String[]{"en"};
        // set doNotStore to true for full privacy of requests, note this doesn't matter for these tests as we're mocking the API response
        PerspectiveRequest requestBody = new PerspectiveRequest(comment, requestedAttributes,
                languages, true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(requestBody, headers);
    }

    /**
     * Creates a dummy response object with a probability score equal to the profanityScore
     * parameter passed in
     */
    private ResponseEntity<PerspectiveResponse> createResponse(double profanityScore) {
        PerspectiveResponse body = new PerspectiveResponse();
        body.setLanguages(List.of("en"));
        Score score = new Score();
        score.setValue(profanityScore);
        score.setType("PROBABILITY");
        AttributeScore attributeScore = new AttributeScore();
        attributeScore.setSummaryScore(score);
        body.setAttributeScores(Map.of("PROFANITY", attributeScore));
        ResponseEntity<PerspectiveResponse> response = new ResponseEntity<>(body, HttpStatus.OK);
        return response;
    }

    @Test
    public void Profanity_RunProfanityCheck_ReturnsTrue() {
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(PerspectiveResponse.class))
        ).thenReturn(createResponse(0.9));

        boolean result = profanityChecker.isProfanePerspective(profaneString);
        Assertions.assertTrue(result);
    }

    @Test
    public void NoProfanity_RunProfanityCheck_ReturnsFalse() {
        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(PerspectiveResponse.class)))
                .thenReturn(createResponse(0.1));

        boolean result = profanityChecker.isProfanePerspective(cleanString);
        Assertions.assertFalse(result);
    }


}
