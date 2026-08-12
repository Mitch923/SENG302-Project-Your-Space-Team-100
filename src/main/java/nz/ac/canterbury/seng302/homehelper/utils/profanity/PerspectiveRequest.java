package nz.ac.canterbury.seng302.homehelper.utils.profanity;

import java.util.Map;

/**
 *
 */
public class PerspectiveRequest {

    private final boolean doNotStore;
    private Comment comment;
    private String[] languages;
    private Map<String, Object> requestedAttributes;

    public PerspectiveRequest(Comment comment, Map<String, Object> requestedAttributes,
            String[] languages, boolean doNotStore) {
        this.comment = comment;
        this.languages = languages;
        this.requestedAttributes = requestedAttributes;
        this.doNotStore = doNotStore;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public String[] getLanguages() {
        return languages;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }

    public Map<String, Object> getRequestedAttributes() {
        return requestedAttributes;
    }

    public void setRequestedAttributes(Map<String, Object> requestedAttributes) {
        this.requestedAttributes = requestedAttributes;
    }
}
