package nz.ac.canterbury.seng302.homehelper.dto;

public class ChunkUploadRequest {

    private String tempUploadToken;
    private Integer chunkIndex;
    private Long startByte;
    private Long endByte;
    private Long totalSize;
    private Integer totalChunks;
    private String originalFileName;
    private String mimeType;

    public ChunkUploadRequest() {

    }

    public String getTempUploadToken() {
        return tempUploadToken;
    }

    public void setTempUploadToken(String tempUploadToken) {
        this.tempUploadToken = tempUploadToken;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Long getStartByte() {
        return startByte;
    }

    public void setStartByte(Long startByte) {
        this.startByte = startByte;
    }

    public Long getEndByte() {
        return endByte;
    }

    public void setEndByte(Long endByte) {
        this.endByte = endByte;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

}
