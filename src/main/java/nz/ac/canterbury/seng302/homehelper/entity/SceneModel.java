package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Class that represents a 3D model that stores a reference to the file in the file system
 */
@Entity
public class SceneModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false)
    private String modelPath;

    @Column(nullable = false)
    private String modelImagePath;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    @Column
    private Boolean emissive; // Contains a light source
    @Column
    private String parallelTexturePath;

    public SceneModel() {

    }

    public SceneModel(String name, User user, String modelPath, String modelImagePath) {
        this.name = name;
        this.user = user;
        this.modelPath = modelPath;
        this.modelImagePath = modelImagePath;
        this.emissive = false;
    }

    public SceneModel(String name, User user, String modelPath, String modelImagePath,
            Boolean emissive) {
        this.name = name;
        this.user = user;
        this.modelPath = modelPath;
        this.modelImagePath = modelImagePath;
        this.emissive = emissive;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getModelImagePath() {
        return modelImagePath;
    }

    public void setModelImagePath(String modelImagePath) {
        this.modelImagePath = modelImagePath;
    }

    public String getImageFileName() {
        return Paths.get(modelImagePath).getFileName().toString();
    }

    public String getModelFileName() {
        return Paths.get(modelPath).getFileName().toString();
    }

    public Boolean getEmissive() {
        return emissive;
    }

    public void setEmissive(Boolean emissive) {
        this.emissive = emissive;
    }

    public String getParallelTexturePath() {
        return parallelTexturePath;
    }

    public void setParallelTexturePath(String parallelTexturePath) {
        this.parallelTexturePath = parallelTexturePath;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SceneModel that = (SceneModel) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
