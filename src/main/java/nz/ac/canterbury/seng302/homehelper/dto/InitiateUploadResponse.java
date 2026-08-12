package nz.ac.canterbury.seng302.homehelper.dto;

public class InitiateUploadResponse {

    private String tempUploadToken;
    private String message;
    private Integer timeoutMinutes;

    public InitiateUploadResponse() {

    }

    public Integer getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public void setTimeoutMinutes(Integer timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTempUploadToken() {
        return tempUploadToken;
    }

    public void setTempUploadToken(String tempUploadToken) {
        this.tempUploadToken = tempUploadToken;
    }
}
