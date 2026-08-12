package nz.ac.canterbury.seng302.homehelper.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The controller for adding and displaying tags
 */
@Controller
public class TagController {

    private final TagService tagService;
    private final RenovationService renovationService;
    private final UserService userService;
    Logger logger = LoggerFactory.getLogger(TagController.class);

    @Autowired
    public TagController(TagService tagService, RenovationService renovationService,
            UserService userService) {
        this.tagService = tagService;
        this.renovationService = renovationService;
        this.userService = userService;
    }

    /**
     * Adds a tag to the given renovation record
     *
     * @param id      the id of the given renovation record
     * @param tagName the name of the tag to be added
     * @return redirect to the view renovation tag page, now with the new tag
     */
    @PostMapping("viewRenovation/addTags/{id}")
    public String addTags(@PathVariable long id,
            @RequestParam("tagName") String tagName,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (!userService.userOwnsRecord(renovationService.getRenovationRecordById(id))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not own this renovation record");
        }
        logger.info("POST /viewRenovation/addTags/{}", id);

        String tag = tagName.trim();
        RenovationRecord renovationRecord = renovationService.getRenovationRecordById(id);

        HashMap<String, String> errors = new HashMap<>();
        tagService.validateTag(errors, renovationRecord, tag);

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("errors", errors);
            redirectAttributes.addFlashAttribute("tagName", tagName);
            return "redirect:/viewRenovation/" + id;
        }

        Optional<Tag> tagObj;
        if (tagService.existsByName(tagName)) {
            tagObj = tagService.findByName(tagName);
        } else {
            tagObj = tagService.save(new Tag(tag.toLowerCase()));
        }
        if (renovationRecord != null && tagObj.isPresent()) {
            renovationRecord.addTag(tagObj.get());
            renovationService.save(renovationRecord);
        }
        return "redirect:/viewRenovation/" + id;
    }

    /**
     * Removes a tag from the given renovation record
     *
     * @param renoId the id of the given renovation record
     * @param tagId  the id of the tag to remove
     * @return redirecteds to the view renovation tag page, now with the new tag
     */
    @PostMapping("viewRenovation/{renoId}/remove-tag/{tagId}")
    public String removeTag(@PathVariable long renoId,
            @PathVariable long tagId) {

        logger.info("POST /viewRenovation/{}/remove-tag/{}", renoId, tagId);

        if (!userService.userOwnsRecord(renovationService.getRenovationRecordById(renoId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not own this renovation record");
        }

        if (tagService.existsById(tagId)) {
            renovationService.removeTagFromRenovationRecord(renoId, tagId);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag does not exist");
        }
        return "redirect:/viewRenovation/" + renoId;
    }

    /**
     * Gets a list of tags whose name start with the supplied prefix. Used for the autocomplete when
     * adding tags
     *
     * @param prefix The string each tag name should start with
     * @return A list of tag names that start with the prefix
     */
    @ResponseBody
    @GetMapping("/getMatchingTags")
    public ResponseEntity<List<String>> getMatchingTags(
            @RequestParam("prefix") String prefix) {
        logger.info("GET /viewRenovation/getMatchingTags");

        List<Tag> tags = tagService.getTagsStartingWith(prefix);
        List<String> tagNames = tags.stream().map(Tag::getName).toList();

        return ResponseEntity.ok(tagNames);
    }

}
