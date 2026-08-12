package nz.ac.canterbury.seng302.homehelper.repository;


import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.SceneTexture;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SceneTextureRepository extends CrudRepository<SceneTexture, Long> {

    List<SceneTexture> findAllByUser(User user);

    List<SceneTexture> findSceneTexturesByName(String name);
}
