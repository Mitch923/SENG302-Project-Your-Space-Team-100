package nz.ac.canterbury.seng302.homehelper.utils;

import java.time.LocalDateTime;

public class TempFileTracker {

    private final LocalDateTime creationDate;
    private final int expirationMinutes;
    private boolean isExpired = false;
    private int totalChunks;
    private int currentChunks;
    private Long designId;
    private boolean isCompetition;

    public TempFileTracker(int expirationMinutes) {
        this.creationDate = LocalDateTime.now();
        this.expirationMinutes = expirationMinutes;
    }

    public Long getDesignId() {
        return designId;
    }

    public void setDesignId(Long designId) {
        this.designId = designId;
    }

    public boolean isCompetition() {
        return isCompetition;
    }

    public void setCompetition(boolean competition) {
        isCompetition = competition;
    }

    public int getCurrentChunks() {
        return currentChunks;
    }

    public void setCurrentChunks(int currentChunks) {
        this.currentChunks = currentChunks;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(int totalChunks) {
        this.totalChunks = totalChunks;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public boolean isCompleted() {
        return currentChunks >= totalChunks;
    }

    /**
     * Checks if {@Code expirationMinutes} have passed since creation and sets isExpired flag
     * accordingly
     *
     * @return
     */
    public boolean expiredCheck() {
        if (LocalDateTime.now().isAfter(creationDate.plusMinutes(expirationMinutes))) {
            this.isExpired = true;
        }
        return this.isExpired;
    }
}
