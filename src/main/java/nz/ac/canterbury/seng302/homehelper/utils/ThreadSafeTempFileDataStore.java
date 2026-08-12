package nz.ac.canterbury.seng302.homehelper.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ThreadSafeTempFileDataStore {

    private final ConcurrentHashMap<String, TempFileTracker> objects = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TempFileTracker> expired = new ConcurrentHashMap<>();

    public void add(String token, TempFileTracker object) {
        objects.put(token, object);
    }

    public Collection<TempFileTracker> getObjects() {
        return objects.values();
    }

    public TempFileTracker get(String token) {
        return objects.get(token);
    }

    public Collection<String> getTokens() {
        return objects.keySet();
    }

    public List<String> getTokenByDesignId(Long designId) {
        List<String> returnKeys = new ArrayList<>();
        objects.forEach((key, value) -> {
            if (value.getDesignId().equals(designId)) {
                returnKeys.add(key);
            }
        });
        return returnKeys;
    }

    public TempFileTracker remove(String token) {
        return objects.remove(token);
    }

    public Collection<String> getExpiredTokens() {
        return expired.keySet();
    }

    public void clearExpiredObjects() {
        expired.clear();
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void cleanup() {
        objects.forEach((key, value) -> {
            if (value.expiredCheck()) {
                expired.put(key, objects.remove(key));
            }
        });
    }
}
