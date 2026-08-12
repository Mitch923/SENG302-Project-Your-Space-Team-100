package nz.ac.canterbury.seng302.homehelper.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.SceneModel;
import nz.ac.canterbury.seng302.homehelper.entity.SceneTexture;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RoomRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.LocationValidator;
import nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RenovationService {

    private final RenovationRecordRepository renovationRecordRepository;
    private final TagService tagService;
    private final SceneModelService sceneModelService;
    private final SceneTextureService sceneTextureService;
    private final TagRepository tagRepository;
    private final RoomRepository roomRepository;
    private final UserService userService;
    Logger logger = LoggerFactory.getLogger(RenovationService.class);
    @Value("${static.resource.folder}")
    private String folder;

    @Autowired
    public RenovationService(RenovationRecordRepository renovationRecordRepository,
            TagService tagService, RoomRepository roomRepository, TagRepository tagRepository,
            SceneModelService sceneModelService, SceneTextureService sceneTextureService,
            UserService userService) {
        this.renovationRecordRepository = renovationRecordRepository;
        this.tagService = tagService;
        this.roomRepository = roomRepository;
        this.tagRepository = tagRepository;
        this.sceneModelService = sceneModelService;
        this.sceneTextureService = sceneTextureService;
        this.userService = userService;
    }

    //Renovation names for creating default records
    private static List<String> renovationRecordNames() {
        return List.of(
                "Renovation Record 1",
                "Renovation Record 2",
                "Renovation Record 3",
                "Renovation Record 4",
                "Renovation Record 5",
                "Renovation Record 6",
                "Renovation Record 7",
                "Renovation Record 8",
                "Renovation Record 9",
                "Renovation Record 10"
        );
    }

    //Design names for creating default designs for some of the renovations
    private static List<String> renovationRecordDescriptions() {
        return List.of(
                "Renovating Kitchen",
                "Bathroom upgrade",
                "Adding a new guest bedroom",
                "Replacing old flooring",
                "Complete repainting",
                "Roof maintenance",
                "Installing new windows",
                "Expanding backyard patio",
                "Upgrading electrical wiring",
                "Modernizing with smart devices"
        );
    }

    // Rooms for creating default renovations
    private static List<List<String>> renovationRecordRooms() {
        return List.of(
                List.of("Kitchen", "Dining Room"),
                List.of("Master Bathroom", "Guest Bathroom"),
                List.of("Guest Bedroom", "Hallway", "Closet"),
                List.of("Living Room", "Hallway", "Dining Room", "Bedroom"),
                List.of("Entire House"),
                List.of("Roof", "Attic"),
                List.of("Office", "Living Room", "Bedroom", "Dining Room", "Sunroom"),
                List.of("Backyard", "Patio", "Garden", "Outdoor Kitchen"),
                List.of("Basement", "Garage", "Kitchen", "Living Room", "Bathroom", "Office"),
                List.of("Living Room", "Kitchen", "Bedroom", "Office", "Hallway", "Garage")
        );
    }

    private static List<List<String>> renovationRecordDesigns() {
        return List.of(
                List.of("Strip the paint", "Prime the walls", "Paint the walls"),
                List.of("Choose the flooring material"),
                List.of("Design 1", "Design 2", "Design 3", "Design 4", "Design 5", "Design 6",
                        "Design 7",
                        "Design 8",
                        "Design 9"),
                List.of("Build a bird house"),
                List.of("Do 10 jumping jacks"),
                List.of("Do 10 jumping jacks", "Do 10 jumping jacks", "Do 10 jumping jacks",
                        "Do 10 jumping jacks", "Do 10 jumping jacks", "Do 10 jumping jacks",
                        "Do 10 jumping jacks", "Do 10 jumping jacks", "Do 10 jumping jacks",
                        "Do 10 jumping jacks", "Do 10 jumping jacks", "Do 10 jumping jacks"),
                List.of("Knock the house down with a wrecking ball"),
                List.of("Eat my breakfast", "Eat my lunch", "Eat my dinner", "Eat my dessert"),
                List.of("Have a nap"),
                List.of("This is a great name for a design")
        );
    }

    /**
     * Method filled in by ChatGPT Edits the list in place to replace any room ids that are -1 with
     * null. This is to allow the controller to be tested using MockMVC as it doesn't support null
     * parameters
     *
     * @param roomIds the list of room ids to convert
     */
    public static void convertNullRoomIds(List<Long> roomIds) {
        if (roomIds == null) {
            return;
        }

        for (int i = 0; i < roomIds.size(); i++) {
            if (roomIds.get(i) != null && roomIds.get(i) == -1L) {
                roomIds.set(i, null);
            }
        }
    }

    /**
     * Method filled in by ChatGPT. Ensures that the given lists are the same size by padding which
     * ever list is smaller with null. This is needed because a list of a singular null roomId will
     * convert to a null rather than a list containing null.
     *
     * @param roomIds   the list of roomIds
     * @param roomNames the list of roomNames
     */
    public static void ensureIdsNameLengthsEqual(List<Long> roomIds, List<String> roomNames) {
        if (roomIds == null || roomNames == null) {
            return;
        }

        int maxLength = Math.max(roomNames.size(), roomIds.size());
        while (roomNames.size() < maxLength) {
            roomNames.add(null);
        }
        while (roomIds.size() < maxLength) {
            roomIds.add(null);
        }
    }

    // Javadoc generated by ChatGPT

    /**
     * Create default renovations and designs to populate default accounts with data
     */
    public void createDefaultRenovations(List<User> users) {

        Tag tag = new Tag("blimey!");
        tagRepository.save(tag);
        User user;
        for (int i = 0; i < renovationRecordNames().size(); i++) {
            user = users.get(0);
            RenovationRecord record = new RenovationRecord(user, renovationRecordNames().get(i),
                    renovationRecordDescriptions().get(i));
            List<Room> rooms = new ArrayList<>();
            for (int j = 0; j < renovationRecordRooms().get(i).size(); j++) {
                rooms.add(new Room(renovationRecordRooms().get(i).get(j), null));
            }
            for (int k = 0; k < renovationRecordDesigns().get(i).size(); k++) {
                // Description generated by ChatGPT
                RenovationDesign renovationDesign = new RenovationDesign(
                        renovationRecordDesigns().get(i).get(k),
                        "A modern, minimalist space featuring neutral colors, warm wood accents, and clean lines, all bathed in natural light for a calm, airy feel.",
                        record,
                        rooms.getFirst()
                );
                record.addDesign(renovationDesign);
            }
            if (i % 2 == 0) {
                record.setPublicRecord(true);
            }
            if (i == 2) {
                for (int j = 10; j < 100; j++) {
                    RenovationDesign renovationDesign = new RenovationDesign(
                            "Design " + j,
                            "Prepare the room for new flooring installation by removing old materials, including carpets, tiles, or hardwood. Clear the area of furniture and debris, and inspect the subfloor for damage or irregularities. Repair any cracks or uneven surfaces to ensure a smooth foundation. Clean thoroughly to remove dust and residue. Measure and mark guidelines for precise placement. Gather necessary tools and materials, ensuring proper ventilation and safety equipment before proceeding with the new flooring installation.",
                            record,
                            rooms.getLast()
                    );
                    record.addDesign(renovationDesign);
                }
                Tag tag1 = new Tag("oops!?");
                Tag tag2 = new Tag("fixingstuff");
                Tag tag3 = new Tag("#skills");
                Tag tag4 = new Tag("āēīōū");

                record.addTag(tag1);
                record.addTag(tag2);
                record.addTag(tag3);
                record.addTag(tag4);

                tag1.addRenovation(record);
                tag2.addRenovation(record);
                tag3.addRenovation(record);
                tag4.addRenovation(record);

            }
            record.addRooms(rooms);
            record = renovationRecordRepository.save(record);
            record.addTag(tag);
            tag.addRenovation(record);
            renovationRecordRepository.save(record);
        }

        for (int i = renovationRecordNames().size() + 1; i < 300; i++) {
            user = users.get(users.size() * i / 300);
            RenovationRecord record = new RenovationRecord(user, "Renovation Record " + i,
                    renovationRecordDescriptions().get(i % renovationRecordDescriptions().size()));
            List<Room> rooms = new ArrayList<>();
            for (int j = 0;
                    j < renovationRecordRooms().get((i + 3) % renovationRecordRooms().size())
                            .size();
                    j++) {
                rooms.add(new Room(
                        renovationRecordRooms().get((i + 3) % renovationRecordRooms().size())
                                .get(j),
                        null));
            }
            if (i % 2 == 0) {
                record.setPublicRecord(true);
            }
            record.addRooms(rooms);
            renovationRecordRepository.save(record);
        }
        sceneModelService.saveModelDetails(
                new SceneModel("CHICKEN", users.get(0),
                        "uploads/models/chicken_joe/chicken_joe.glb",
                        "uploads/model-images/chicken_joe.png"));
        sceneModelService.saveModelDetails(
                new SceneModel("SUPER COOL CHAIR", null,
                        "uploads/models/desk_chair/scene.gltf",
                        "uploads/model-images/cool_chair.png"));
        sceneModelService.saveModelDetails(
                new SceneModel("Orange Cat 🐈", users.get(0), "uploads/models/garfield/scene.gltf",
                        "uploads/model-images/garfield.png"));
        sceneModelService.saveModelDetails(
                new SceneModel("Plain Chair", null, "uploads/models/chair/result.gltf",
                        "uploads/model-images/chair.png"));

        // Default data for custom textures
        sceneTextureService.saveSceneTexture(
                new SceneTexture("Fab", users.get(0),
                        UploadDirectory.CUSTOM_TEXTURES.getRelativePath()
                                .resolve("fab.png").toString())
        );

        Path system = Paths.get(System.getProperty("user.dir"));

// ---- MODELS ----
        Path modelPath = Paths.get(system.toString(), folder, "models/default-models");
        logger.info("model path: " + modelPath);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modelPath)) {
            for (Path entry : stream) {
                String filename = entry.getFileName().toString();

                Optional<Path> model = Files.list(entry)
                        .filter(file -> file.getFileName().toString().endsWith(".glb"))
                        .findFirst();

                Optional<Path> previewImage = Files.list(entry)
                        .filter(file -> file.getFileName().toString().endsWith(".png"))
                        .findFirst();

                model.ifPresent(value -> {
                    // Convert physical path to app path
                    Path relativeModel = system.relativize(value);
                    String modelPathStr = relativeModel.toString().replace(folder, "uploads");

                    String previewPathStr;
                    if (previewImage.isEmpty()) {
                        previewPathStr = "img/test-image.png";
                    } else {
                        Path relativePreview = system.relativize(previewImage.get());
                        previewPathStr = relativePreview.toString().replace(folder, "uploads");
                    }
                    if (filename.contains("Floor Lamp")
                            || filename.contains("pendant")
                            || filename.contains("tomon")) {
                        sceneModelService.saveModelDetails(
                                new SceneModel(FileUtilities.capitalizeWords(filename), null,
                                        modelPathStr, previewPathStr, true));
                    } else {
                        sceneModelService.saveModelDetails(
                                new SceneModel(FileUtilities.capitalizeWords(filename), null,
                                        modelPathStr, previewPathStr,
                                        false));
                    }
                });
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

// ---- TEXTURES ----
        Path texturePath = Paths.get(system.toString(), folder, "textures/default-textures");
        logger.info("texture path: " + texturePath);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(texturePath)) {
            for (Path entry : stream) {
                String filename = entry.getFileName().toString();

                Optional<Path> model = Files.list(entry)
                        .filter(file -> file.getFileName().toString().endsWith("Color.jpg"))
                        .findFirst();

                model.ifPresent(value -> {
                    Path relative = system.relativize(value);
                    String filePathStr = relative.toString().replace(folder, "uploads");

                    sceneTextureService.saveSceneTexture(
                            new SceneTexture(filename, null, filePathStr));
                });
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    // Javadoc generated by ChatGPT

    /**
     * Adds a new RenovationRecord with associated rooms to the repository. Only adds rooms if the
     * rooms list has room(s) in it and no rooms have been added to the record already.
     *
     * @param renovationRecord the RenovationRecord to be added
     * @param rooms            the list of rooms to be associated with the RenovationRecord
     * @return the saved RenovationRecord with the associated rooms
     */
    public RenovationRecord addRenovationRecord(RenovationRecord renovationRecord,
            List<Room> rooms) {
        RenovationRecord createdRenovationRecord = renovationRecordRepository.save(
                renovationRecord);
        if (rooms != null && !rooms.isEmpty() && createdRenovationRecord.getRooms().isEmpty()) {
            logger.info("Adding Rooms");
            addRoomsToRenovationRecord(rooms, createdRenovationRecord);
        }
        return createdRenovationRecord;
    }

    // Javadoc generated by ChatGPT

    /**
     * Retrieves all RenovationRecords belonging to the user from the repository.
     *
     * @return a list of all RenovationRecords the user has created
     */
    public List<RenovationRecord> getRenovationRecords(User user) {
        return renovationRecordRepository.findAllByUser(user);
    }

    /**
     * Associates a list of rooms with a RenovationRecord and saves it.
     *
     * @param rooms            the list of rooms to be added
     * @param renovationRecord the RenovationRecord to which the rooms will be added
     * @return the saved RenovationRecord with the added rooms
     */
    public RenovationRecord addRoomsToRenovationRecord(List<Room> rooms,
            RenovationRecord renovationRecord) {
        // Adding the rooms to the renovation record object and then saving the renovation record persists the rooms
        renovationRecord.addRooms(rooms);
        return renovationRecordRepository.save(renovationRecord);
    }

    /**
     * Retrieves the renovation record with the specific id from the repository
     *
     * @param id the unique id of the renovation record
     * @return the RenovationRecord with that id
     */
    public RenovationRecord getRenovationRecordById(long id) {
        return renovationRecordRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves the renovation record with the specific name from the repository
     *
     * @param name the unique name of the renovation record
     * @return the RenovationRecord with that name
     */
    public RenovationRecord getRenovationRecordByName(String name) {
        return renovationRecordRepository.findRenovationByName(name);
    }

    /**
     * Retrieves a list of renovation records with a name or description that contains the given
     * query substring
     *
     * @param query      the string search query, null to retrieve all records
     * @param user       user's records to search, null to search public records
     * @param pageNumber which page of records to retrieve (1-indexed)
     * @param pageSize   number of records to retrieve
     * @return list of matching  renovation records
     */
    public Page<RenovationRecord> searchRenovationRecords(String query, User user, int pageSize,
            int pageNumber) {
        query = query == null ? "" : query;
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        return renovationRecordRepository.searchByNameOrDescription(query, user, pageable);
    }

    /**
     * Retrieves all public renovation records. Overloaded to make it more intuitive to search for
     * all public renovation records
     *
     * @return list of public renovation records
     */
    public List<RenovationRecord> searchRenovationRecords() {
        return renovationRecordRepository.searchByNameOrDescription(null, null, null).getContent();
    }

    /**
     * Validates that there is not already a renovation record saved with this name
     *
     * @param name The renovation record name
     * @return Empty string if no error, or an error message if there is an error
     */
    public String validateUniqueRenovationRecord(String name) {
        String error = "";
        RenovationRecord matchingRenovationRecord = getRenovationRecordByName(name);
        if (!(matchingRenovationRecord == null)) {
            error = "Renovation record name is not unique";
        }
        return error;
    }

    /**
     * Validates that there is not already a renovation record saved with this name apart from the
     * renovation with the given id. If there is, adds an error message to the given hashmap
     *
     * @param errors       The hashmap to add the error to
     * @param name         The renovation record name
     * @param renovationId the id of the renovation that can have that name
     */
    public void validateUniqueRenovationRecord(HashMap<String, String> errors, String name,
            Long renovationId) {
        RenovationRecord matchingRenovationRecord = getRenovationRecordByName(name);
        if (!(matchingRenovationRecord == null || renovationId.equals(
                matchingRenovationRecord.getId()))) {
            errors.put("duplicate", "Renovation record name is not unique");
        }
    }

    /**
     * Deletes a RenovationRecord with the given id
     *
     * @param id Long, the id of the renovation record to be deleted
     */
    public void deleteRenovationRecordById(long id) {
        renovationRecordRepository.deleteById(id);
    }

    /**
     * Used to change the isPublic attribute of a renovation record based on its id
     *
     * @param id         Id of the renovation record to update
     * @param visibility boolean value for if the renovation should be public
     */
    public void setVisibility(long id, boolean visibility) {
        RenovationRecord renovationRecord = getRenovationRecordById(id);
        renovationRecord.setPublicRecord(visibility);
        renovationRecordRepository.save(renovationRecord);
    }

    /**
     * Gets a list of all the tags applied to a given renovation record Helped by ChatGPT
     *
     * @param id the id of the renovation record
     * @return a list of the tags
     */
    public List<Tag> getTagsByRenovationId(long id) {
        return renovationRecordRepository.findById(id)
                .map(RenovationRecord::getTags)
                .orElse(Collections.emptyList()); // Return an empty list if the Optional is empty
    }

    /**
     * Adds or update the renovation record to the repository
     *
     * @param renovationRecord the renovation record to update or add
     * @return the saved or updated renovation record
     */
    public RenovationRecord save(RenovationRecord renovationRecord) {
        return renovationRecordRepository.save(renovationRecord);
    }

    public void removeTagFromRenovationRecord(Long recordId, Long tagId) {
        RenovationRecord renovationRecord = getRenovationRecordById(recordId);
        Optional<Tag> tag = tagService.findById(tagId);
        tag.get().removeRenovation(renovationRecord);
        renovationRecord.removeTag(tag.get());
        tagService.pruneTag(tagId);
        save(renovationRecord);
    }

    /**
     * Saves a location to a renovation record, or removes a location if passed an empty location or
     * null.
     *
     * @param renovationRecord The renovation record to save the location to.
     * @param location         The new Location to be saved, or null to remove a saved location.
     */
    public void setRenovationLocation(RenovationRecord renovationRecord, Location location) {
        if (!LocationValidator.isLocationEmpty(location)) {
            renovationRecord.setRenovationLocation(location);
            save(renovationRecord);
        } else {
            renovationRecord.setRenovationLocation(null);
            save(renovationRecord);
        }
    }

    /**
     * Find the page number of a given renovation from the query based on the page size
     *
     * @param id       the id of the renovation
     * @param pageSize the size of the page
     * @param query    the input query
     * @param user     the user that is querying
     * @return the page of the renovation with the given id
     */
    public int findPageNumberOfId(long id, int pageSize, String query, User user) {
        Integer index = renovationRecordRepository.findPageNumberById(id, query, user);
        if (index == null) {
            index = 1;
        }
        return (index - 1) / pageSize + 1;
    }

    /**
     * Updates the rooms in the renovation record to be the new list of rooms. Removes rooms from
     * the renovation record if their id is not in the list of room ids. Adds new rooms to the
     * renovation record
     *
     * @param renovationRecord the record to be updated
     * @param roomNames        the list of room names
     * @param roomIds          the list of room ids that correspond with the room names, a room's id
     *                         is null if it is new
     */
    public void updateRoomsInRenovationRecord(RenovationRecord renovationRecord,
            List<String> roomNames, List<Long> roomIds) {
        List<Room> removedRooms = new ArrayList<>();
        for (Room room : renovationRecord.getRooms()) {
            if (!roomIds.contains(room.getId())) {
                removedRooms.add(room);
            }
        }

        for (Room removedRoom : removedRooms) {
            renovationRecord.getRooms().remove(removedRoom);
        }

        for (int i = 0; i < roomNames.size(); i++) {
            if (roomIds.get(i) == null) {
                Room newRoom = new Room(roomNames.get(i), renovationRecord);
                renovationRecord.getRooms().add(newRoom);
            }
        }
    }

    public Room getRoomById(Long id) {
        if (id == null) {
            return null;
        }
        return roomRepository.findById(id).orElse(null);
    }

    public List<RenovationRecord> getRenovationRecordsByNameSubstring(String substring) {
        User user = userService.getLoggedUser();
        if (user == null) {
            return List.of();
        } else {
            return renovationRecordRepository.findByUserAndNameContainingIgnoreCase(user, substring);
        }
    }

}
