package nz.ac.canterbury.seng302.homehelper.dto;

/**
 * A DTO used to transfer design metadata from the frontend.
 * This class is used to bind JSON fields (name, description, designRoomId)
 * from a multipart/form-data request when submitted alongside a file upload.
 * Spring automatically maps the "json" part of the request to this object.
 */
public class DesignDataDTO {

    private String name;
    private String description;
    private String designRoomId;

    public DesignDataDTO() {
    }

    public DesignDataDTO(String name, String description, String designRoomId) {
        this.name = name;
        this.description = description;
        this.designRoomId = designRoomId;
    }

    public String getDesignRoomId() {
        return designRoomId;
    }

    public void setDesignRoomId(String designRoomId) {
        this.designRoomId = designRoomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

