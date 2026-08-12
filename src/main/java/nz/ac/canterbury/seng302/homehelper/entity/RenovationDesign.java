package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;

/**
 * Class that represents a design for a renovation in the database. Each design is linked to one
 * room Each design can be assigned to 1 or more rooms
 */
@Entity
public class RenovationDesign {

    private final boolean isRenovationDesign = true;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 512)
    private String name;
    @Column(nullable = false, length = 1024)
    private String description;
    @ManyToOne
    @JoinColumn(name = "renovation_record_id", nullable = false)
    private RenovationRecord relatedRenovationRecord;
    @ManyToOne()
    @JoinTable(
            name = "design_room",
            joinColumns = @JoinColumn(name = "design_id"))
    private Room relatedRoom;
    @Column
    private String iconName;
    @Column
    private String thumbnailFileName; // Thumbnail of design

    @Column
    private String sceneChunkDirectory;

    @Column
    private Integer chunkCount;

    /**
     * JPA required no-args constructor
     */
    protected RenovationDesign() {
    }

    public RenovationDesign(String name, String description,
            RenovationRecord relatedRenovationRecord, Room relatedRoom) {
        this.name = name;
        this.description = description;
        this.relatedRenovationRecord = relatedRenovationRecord;
        this.relatedRoom = relatedRoom;
        this.iconName = null;
        this.sceneChunkDirectory = "";
    }

    public RenovationDesign(String name, String description,
            RenovationRecord relatedRenovationRecord) {
        this.name = name;
        this.description = description;
        this.relatedRenovationRecord = relatedRenovationRecord;
        this.sceneChunkDirectory = "";
    }

    public RenovationDesign(String name, String description, Room room) {
        this.name = name;
        this.description = description;
        this.relatedRoom = room;
        this.sceneChunkDirectory = "";
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public String getSceneChunkDirectory() {
        return sceneChunkDirectory;
    }

    public void setSceneChunkDirectory(String sceneChunkDirectory) {
        this.sceneChunkDirectory = sceneChunkDirectory;
    }

    public String getThumbnailFileName() {
        return thumbnailFileName;
    }

    public void setThumbnailFileName(String fileName) {
        this.thumbnailFileName = fileName;
    }

    public RenovationRecord getRelatedRenovationRecord() {
        return relatedRenovationRecord;
    }

    public void setRelatedRenovationRecord(RenovationRecord relatedRenovationRecord) {
        this.relatedRenovationRecord = relatedRenovationRecord;
    }

    public String getName() {
        return name;
    }

    public Room getRelatedRoom() {
        return relatedRoom;
    }

    public void setRelatedRoom(Room relatedRoom) {
        this.relatedRoom = relatedRoom;
    }

    public RenovationRecord getRenovationRecord() {
        return relatedRenovationRecord;
    }

    public void setRenovationRecord(RenovationRecord renovationRecord) {
        this.relatedRenovationRecord = renovationRecord;
    }

    public String getDescription() {
        return description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIconName() {
        return iconName;
    }

    public void updateDesign(RenovationDesign renovationDesign) {
        this.name = renovationDesign.name;
        this.description = renovationDesign.description;
        this.relatedRoom = renovationDesign.relatedRoom;
    }

    public boolean isRenovationDesign() {
        return isRenovationDesign;
    }
}
