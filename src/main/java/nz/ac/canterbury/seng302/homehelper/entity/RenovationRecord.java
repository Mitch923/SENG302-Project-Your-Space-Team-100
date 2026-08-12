package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

/**
 * Class that represents a renovation record in the database. Each renovation can have multiple
 * rooms Each renovation can have multiple designs. A renovation can be associated with only one
 * location.
 */
@Entity
public class RenovationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String name;

    @Column(nullable = false, length = 1024)
    private String description;

    @OneToMany(mappedBy = "renovationRecord", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "relatedRenovationRecord", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RenovationDesign> designsForRenovation;

    @Column(nullable = false)
    private boolean publicRecord = false;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "renovation_location",
            joinColumns = {@JoinColumn(name = "renovation_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "location_id", referencedColumnName = "id")})
    private Location renovationLocation;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "tag_renovation_records",
            joinColumns = @JoinColumn(name = "renovation_record_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"renovation_record_id", "tag_id"}))
    private List<Tag> tags;

    /**
     * JPA required no-args constructor
     */
    public RenovationRecord() {
    }

    public RenovationRecord(User user, String name, String description) {
        this.user = user;
        this.name = name;
        this.description = description;
        this.rooms = new ArrayList<>();
        this.designsForRenovation = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public RenovationRecord(String name, String description) {
        this.name = name;
        this.description = description;
        this.rooms = new ArrayList<>();
        this.designsForRenovation = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public void addRooms(List<Room> rooms) {
        this.rooms.addAll(rooms);
        setRoomsIds();
    }

    public void replaceRooms(List<Room> rooms) {
        this.rooms.clear();
        this.addRooms(rooms);
    }

    public void setRoomsIds() {
        for (Room room : this.rooms) {
            room.setRenovationRecord(this);
        }
    }

    public List<RenovationDesign> getDesignsForRenovation() {
        return designsForRenovation;
    }

    public void setDesignsForRenovation(
            List<RenovationDesign> designsForRenovation) {
        this.designsForRenovation = designsForRenovation;
    }

    public void addDesign(RenovationDesign renovationDesign) {
        this.designsForRenovation.add(renovationDesign);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public boolean isPublicRecord() {
        return publicRecord;
    }

    public void setPublicRecord(boolean publicRecord) {
        this.publicRecord = publicRecord;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        if (!this.tags.contains(tag)) {
            tags.add(tag);
            tag.getRenovations().add(this);
        }
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getRenovations().remove(this);
    }

    public Location getRenovationLocation() {
        return this.renovationLocation;
    }

    public void setRenovationLocation(Location location) {
        this.renovationLocation = location;
    }

    public List<Room> replaceRoomsWithSpringFormatted(List<String> roomNames) {
        List<Room> rooms = new ArrayList<>();

        if (roomNames != null && !roomNames.isEmpty()) {
            List<String> reformattedRoomName = reformatRoomNames(roomNames);
            for (String roomName : reformattedRoomName) {
                Room room = new Room(roomName, this);
                rooms.add(room);
            }
            this.addRooms(rooms);
        }
        return rooms;
    }

    /**
     * Returns a stream of unmodifiable rooms. Unmodifiable rooms are defined as rooms with design
     * related to them.
     *
     * @return unmodifiable rooms
     */
    public Stream<Room> getUnmodifiableRooms() {
        return this.getRooms().stream()
                .filter(room -> !room.getDesignsForRoom().isEmpty());
    }

    /**
     * Returns a list of all room ids that are unmodifiable. Rooms are unmodifiable if they are
     * linked to at least one design.
     *
     * @return {@code List<Long>} unmodifiable room ids
     */
    private List<Long> getUnmodifiableRoomIds() {
        return this.getUnmodifiableRooms().map(Room::getId).toList();
    }

    /**
     * To allow commas in room names commas are turned into exclamation marks to be parsed correctly
     * into strings
     *
     * @param roomNames the modified room names
     * @return reformatted room names
     */
    public List<String> reformatRoomNames(List<String> roomNames) {
        if (roomNames == null) {
            return new ArrayList<>();
        }
        return roomNames.stream().map(room -> room.replace("!", ",")).toList();
    }


    /**
     * Check that a new list of room ids maintains the rooms that cannot be deleted due to having a
     * design related to them. Adds an error to the hashmap if a room has been removed that has a
     * design related to it
     *
     * @param errors  the hashmap to add the error into
     * @param roomIds the names of the new rooms
     */
    public void validateUnmodifiableRoomMaintained(HashMap<String, String> errors,
            List<Long> roomIds) {
        List<Long> unmodifiableRoomIds = this.getUnmodifiableRoomIds();
        if (roomIds == null) {
            roomIds = Collections.emptyList(); // Treat null as empty
        }

        if (!new HashSet<>(roomIds).containsAll(unmodifiableRoomIds)) {
            errors.put("unmodifiable",
                    "Cannot delete rooms that have designs associated with them");
        }
    }
}
