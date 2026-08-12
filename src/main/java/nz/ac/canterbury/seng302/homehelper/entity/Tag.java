package nz.ac.canterbury.seng302.homehelper.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * A database entity that represents a tag. Tags are a name marker that can be added to renovation
 * record to give extra information about them and add keywords to make them easier to search for
 * and find
 */
@Entity
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
    @ManyToMany(mappedBy = "tags")
    private List<RenovationRecord> renovations;

    public Tag(String name) {
        this.renovations = new ArrayList<>();
        this.name = name;
    }

    protected Tag() {
    }

    public String getName() {
        return name;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void addRenovation(RenovationRecord renovation) {
        this.renovations.add(renovation);
    }

    public void removeRenovation(RenovationRecord renovation) {
        this.renovations.remove(renovation);
    }

    public List<RenovationRecord> getRenovations() {
        return renovations;
    }
}
