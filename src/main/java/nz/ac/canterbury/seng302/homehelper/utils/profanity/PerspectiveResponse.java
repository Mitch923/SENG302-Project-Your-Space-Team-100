package nz.ac.canterbury.seng302.homehelper.utils.profanity;

import java.util.List;
import java.util.Map;

public class PerspectiveResponse {

    private Map<String, AttributeScore> attributeScores;
    private List<String> languages;

    public Map<String, AttributeScore> getAttributeScores() {
        return attributeScores;
    }

    public void setAttributeScores(Map<String, AttributeScore> attributeScores) {
        this.attributeScores = attributeScores;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }


}

