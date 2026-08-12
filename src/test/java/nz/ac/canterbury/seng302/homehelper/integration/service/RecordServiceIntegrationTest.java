package nz.ac.canterbury.seng302.homehelper.integration.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class RecordServiceIntegrationTest {

    @Autowired
    private RenovationService renovationService;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private UserService userService;

    private Tag testTag;

    private RenovationRecord testRenovationRecord;

    private RenovationRecord testRenovationRecord2;

    @BeforeEach
    void setUp() {
        User user = new User("jane@doe.nz", "test", "jane", "doe");
        userService.saveUser(user);
        user = userService.getUserByEmail("jane@doe.nz").get();

        testRenovationRecord = new RenovationRecord(user, "test1", "test");
        testRenovationRecord = renovationService.save(testRenovationRecord);

        testRenovationRecord2 = new RenovationRecord(user, "test2", "test");
        testRenovationRecord2 = renovationService.save(testRenovationRecord2);

        testTag = tagRepository.save(new Tag("tag1"));
        testRenovationRecord.addTag(testTag);
        testRenovationRecord = renovationService.save(testRenovationRecord);

        testRenovationRecord2.addTag(testTag);
        testRenovationRecord2 = renovationService.save(testRenovationRecord2);
    }

    @Test
    public void deleteTag_tagExistsUserOwnsRecord_tagDeletedButStillExists() {
        Assertions.assertTrue(testRenovationRecord.getTags().contains(testTag));
        renovationService.removeTagFromRenovationRecord(testRenovationRecord.getId(),
                testTag.getId());
        Assertions.assertFalse(testRenovationRecord.getTags().contains(testTag));

        Assertions.assertTrue(tagRepository.existsByName(testTag.getName()));
    }

    /**
     * Tests that tags are deleted from the database once they are deleted off their final
     * renovation
     */
    @Test
    public void deleteTag_tagExistsUserOwnsRecord_tagDeletedAndPruned() {
        Assertions.assertTrue(tagRepository.existsByName(testTag.getName()));
        renovationService.removeTagFromRenovationRecord(testRenovationRecord.getId(),
                testTag.getId());
        renovationService.removeTagFromRenovationRecord(testRenovationRecord2.getId(),
                testTag.getId());

        Assertions.assertFalse(tagRepository.existsByName(testTag.getName()));
    }

}
