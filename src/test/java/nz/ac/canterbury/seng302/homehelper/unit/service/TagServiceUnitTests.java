package nz.ac.canterbury.seng302.homehelper.unit.service;

import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TagServiceUnitTests {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    public void saveTag_TestExists() {
        Tag savedTag = new Tag("saved");
        Tag savingTag = new Tag("saving");

        Mockito.when(tagRepository.existsByName(Mockito.anyString())).thenReturn(true);
        Mockito.when(tagRepository.findByName(Mockito.anyString()))
                .thenReturn(Optional.of(savedTag));

        Optional<Tag> resultant = tagService.save(savingTag);

        Mockito.verify(tagRepository, Mockito.times(0)).save(Mockito.any(Tag.class));
        Mockito.verify(tagRepository, Mockito.times(1)).findByName(Mockito.anyString());

        Assertions.assertTrue(resultant.isPresent());
        Assertions.assertEquals(savedTag, resultant.get());
    }

    @Test
    public void saveTag_TestNotExists() {
        Tag savingTag = new Tag("saving");

        Mockito.when(tagRepository.existsByName(Mockito.anyString())).thenReturn(false);
        Mockito.when(tagRepository.save(Mockito.any(Tag.class))).thenReturn(savingTag);

        Optional<Tag> resultant = tagService.save(savingTag);

        Mockito.verify(tagRepository, Mockito.times(1)).save(Mockito.any(Tag.class));
        Mockito.verify(tagRepository, Mockito.times(0)).findByName(Mockito.anyString());

        Assertions.assertTrue(resultant.isPresent());
        Assertions.assertEquals(savingTag, resultant.get());
    }
}
