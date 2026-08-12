package nz.ac.canterbury.seng302.homehelper.repository;

import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.SceneModel;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SceneModelRepository extends CrudRepository<SceneModel, Long> {

    List<SceneModel> findSceneModelsByUser(User user);

    SceneModel getByName(String name);

    SceneModel getById(Long id);
}
