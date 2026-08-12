package nz.ac.canterbury.seng302.homehelper.dto;

public class InitiateUploadRequest {

    private Long designId;
    private String fileName;
    private String mimeType;
    private Long totalSize;
    private Integer expectedChunks;
    private boolean isCompetition;

    public boolean isCompetition() {
        return isCompetition;
    }

    public void setIsCompetition(boolean competition) {
        isCompetition = competition;
    }

    public Integer getExpectedChunks() {
        return expectedChunks;
    }

    public void setExpectedChunks(Integer expectedChunks) {
        this.expectedChunks = expectedChunks;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getDesignId() {
        return designId;
    }

    public void setDesignId(Long designId) {
        this.designId = designId;
    }
}
