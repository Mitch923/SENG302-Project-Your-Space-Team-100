package nz.ac.canterbury.seng302.homehelper.dto;

public class ChunkUploadResponse {

    private boolean success;
    private String message;
    private Integer receivedChunks;
    private Integer expectedChunks;
    private boolean uploadComplete;

    public boolean isUploadComplete() {
        return uploadComplete;
    }

    public void setUploadComplete(boolean uploadComplete) {
        this.uploadComplete = uploadComplete;
    }

    public Integer getExpectedChunks() {
        return expectedChunks;
    }

    public void setExpectedChunks(Integer expectedChunks) {
        this.expectedChunks = expectedChunks;
    }

    public Integer getReceivedChunks() {
        return receivedChunks;
    }

    public void setReceivedChunks(Integer receivedChunks) {
        this.receivedChunks = receivedChunks;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setIsSuccess(boolean b) {
        this.success = b;
    }
}
