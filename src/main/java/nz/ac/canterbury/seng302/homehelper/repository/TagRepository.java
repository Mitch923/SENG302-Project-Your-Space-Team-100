package nz.ac.canterbury.seng302.homehelper.repository;

import java.util.List;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends CrudRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT(:prefix, '%'))")
    List<Tag> findTagsByNameStartingWith(@Param("prefix") String prefix);
}
