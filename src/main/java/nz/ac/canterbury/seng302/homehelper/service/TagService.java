package nz.ac.canterbury.seng302.homehelper.service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.utils.profanity.ProfanityChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service class to handle operation for tag such as finding and saving tags
 */
@Service
public class TagService {

    private final static String VALIDTAGREGEX = ".*\\p{L}.*";
    private final TagRepository tagRepository;
    private ProfanityChecker profanityChecker;

    @Autowired
    public TagService(TagRepository tagRepository, ProfanityChecker profanityChecker) {
        this.tagRepository = tagRepository;
        this.profanityChecker = profanityChecker;
    }

    /**
     * Checks that the given tag name is valid to be added into the given renovation record. If not,
     * adds the respective error to a hashmap which is then returned. <br> <br> Also checks for
     * profanity using ProfanityChecker
     *
     * @param record  RenovationRecord to validate the tag against
     * @param tagName String name of the new tag inputted by the user
     */
    public void validateTag(HashMap<String, String> errors, RenovationRecord record,
            String tagName) {
        if (record.getTags() != null) {
            List<String> recordTags = record.getTags().stream().map(Tag::getName)
                    .map(String::toLowerCase).toList();
            if (recordTags.contains(tagName.toLowerCase())) {
                errors.put("tag", "Cannot add the same tag to a Renovation Record more than once");
            }
            if (record.getTags().size() >= 5) {
                errors.put("tag", "Renovation records cannot have more than 5 tags");
            }
            if (profanityChecker.isProfanePerspective(tagName)) {
                errors.put("tag",
                        "The tag entered is profane and does not follow the system language standards");
            }
        }
        if (!tagName.matches(VALIDTAGREGEX)) {
            errors.put("tag", "tags must contain letters");
        }
    }

    public Optional<Tag> save(Tag tag) {
        if (!tagRepository.existsByName(tag.getName())) {
            return Optional.of(tagRepository.save(tag));
        }
        return tagRepository.findByName(tag.getName());
    }

    public Optional<Tag> findByName(String name) {
        return tagRepository.findByName(name);
    }

    public Optional<Tag> findById(Long id) {
        return tagRepository.findById(id);
    }

    public boolean existsByName(String name) {
        return tagRepository.existsByName(name);
    }

    public boolean existsById(Long id) {
        return tagRepository.existsById(id);
    }

    /**
     * Retrieves the tags whose name starts with the supplied prefix. Puts the prefix in lowercase
     * as tag names are stored in all lower case
     *
     * @param prefix the string each tags name should start with
     * @return A list of tags whose names start with the prefix
     */
    public List<Tag> getTagsStartingWith(String prefix) {
        return tagRepository.findTagsByNameStartingWith(prefix.toLowerCase());
    }

    /**
     * Checks if a tag is associated with any renovation, if not, deletes it from the database
     *
     * @param tagId Id of tag to prune
     */
    public void pruneTag(Long tagId) {
        Optional<Tag> optionalTag = findById(tagId);
        if (optionalTag.isPresent()) {
            Tag tag = optionalTag.get();
            if (tag.getRenovations().isEmpty()) {
                tagRepository.delete(tag);
            }
        }
    }

    public void setProfanityChecker(ProfanityChecker profanityChecker) {
        this.profanityChecker = profanityChecker;
    }
}
