package nz.ac.canterbury.seng302.homehelper.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nz.ac.canterbury.seng302.homehelper.entity.Room;

/**
 * Utilities class containing static methods for reformating Room objects
 */
public class RoomUtilities {

    /**
     * Returns a front-end friendly representation of a list of rooms. This allows the rooms to be
     * easily serialised to JSON and injected into JavaScript using Thymeleaf.
     *
     * @param rooms the list of rooms to simplify
     * @return {@code List<Map<String, Object>>} simplified room summary list
     */
    public static List<Map<String, Object>> generateSimplifiedRoomSummaries(List<Room> rooms) {
        return rooms.stream()
                .map(room -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", room.getId());
                    map.put("name", room.getName());
                    map.put("isDeletable", room.isModifiable());
                    return map;
                })
                .toList();
    }
}
