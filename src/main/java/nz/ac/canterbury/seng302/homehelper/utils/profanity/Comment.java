package nz.ac.canterbury.seng302.homehelper.utils.profanity;

public class Comment {

    private String text;

    public Comment(String comment) {
        this.text = comment;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
