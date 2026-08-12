package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a room in the database. Each room can be linked to only one renovation
 * record Each room can be linked to many designs(for the same renovation record)
 */
@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String name;

    @ManyToOne
    @JoinColumn(name = "renovation_record_id", nullable = false)
    private RenovationRecord renovationRecord;

    @OneToMany(mappedBy = "relatedRoom")
    private List<RenovationDesign> designsForRoom;


    /**
     * JPA required no-args constructor
     */
    protected Room() {
    }

    public Room(String name, RenovationRecord renovationRecord) {
        this.name = name;
        this.renovationRecord = renovationRecord;
        this.designsForRoom = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setRenovationRecord(RenovationRecord renovationRecord) {
        this.renovationRecord = renovationRecord;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<RenovationDesign> getDesignsForRoom() {
        return designsForRoom;
    }

    public void addDesign(RenovationDesign renovationDesign) {
        designsForRoom.add(renovationDesign);
    }

    public boolean isModifiable() {
        return designsForRoom.isEmpty();
    }
}
