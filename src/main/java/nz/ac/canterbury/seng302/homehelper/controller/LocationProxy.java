package nz.ac.canterbury.seng302.homehelper.controller;


import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

/**
 * Provides a proxy for the IPapi and Mapbox Geocode API apis
 */
@Controller
public class LocationProxy {

    private final int LIMIT = 5; // The number of autocomplete items mapbox should return
    private final RestTemplate restTemplate = new RestTemplate();
    private final String IPAPI_ENDPOINT = "https://ipapi.co/";
    Logger logger = LoggerFactory.getLogger(LocationProxy.class);
    String mapboxUrlTemplate = "https://api.mapbox.com/search/geocode/v6/forward?q=%s"
            + "&access_token=%s"
            + "&autocomplete=true"
            + "&proximity=%s"
            + "&limit=%s";
    @Value("${mapbox.api.key}")
    private String MAPBOX_KEY;
    @Value("${ipapi.api.key}")
    private String IPAPI_KEY;

    /**
     * Proxy method to be called when an approximate location is required based on the ip of the
     * caller. This provides a proxy for the IPapi api that can return geolocation information based
     * on a provided ip address.
     *
     * @param request The Http request used to retrieve the
     * @return The json response from the IPapi api
     */
    @ResponseBody
    @GetMapping("/getIPGeolocation")
    public ResponseEntity<String> getIPGeolocation(HttpServletRequest request) {
        logger.info("getIPGeolocation");
        try {
            String xfHeader = request.getHeader("X-Forwarded-For");
            String clientIP = (xfHeader != null && !xfHeader.isEmpty())
                    ? xfHeader.split(",")[0].trim()
                    : request.getRemoteAddr();

            InetAddress clientIPAddress = InetAddress.getByName(clientIP);
            if (clientIPAddress.isLoopbackAddress()) {
                logger.info("using fallback ip");
                clientIP = "202.124.110.231"; // Test IP when running on localhost
            }

            String url = IPAPI_ENDPOINT + clientIP + "/json/?key=" + IPAPI_KEY;
            String jsonResponse = restTemplate.getForObject(url, String.class);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.ok().headers(headers).body(jsonResponse);

        } catch (Exception e) {
            String errorJson = String.format(
                    "{\"error\":\"Failed to fetch geolocation\", \"details\":\"%s\"}",
                    e.getMessage().replace("\"", "\\\""));
            return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson);
        }
    }

    /**
     * Proxy method to retrieve addresses for use in autocompletion using the mapbox forward
     * geocoding api.
     *
     * @param query        A search string used by mapbox to filter address results
     * @param userLocation longitude, latitude pair to bias the response for results that are closer
     *                     to this location
     * @return The json response from the mapbox api
     */
    @GetMapping("/getMapboxForwardGeocoding")
    public ResponseEntity<String> getMapboxGeocoding(
            @RequestParam("query") String query,
            @RequestParam("userLocation") String userLocation) {
        logger.info("getMapboxGeocoding");
        try {
            String url = String.format(
                    mapboxUrlTemplate,
                    query,
                    MAPBOX_KEY,
                    userLocation,
                    LIMIT
            );
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null,
                    String.class);
            String jsonResponse = response.getBody();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return ResponseEntity.ok().headers(headers).body(jsonResponse);
        } catch (Exception e) {
            String errorJson = String.format(
                    "{\"error\":\"Failed to fetch suggestions\", \"details\":\"%s\"}",
                    e.getMessage().replace("\"", "\\\""));
            return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON)
                    .body(errorJson);
        }
    }
}
