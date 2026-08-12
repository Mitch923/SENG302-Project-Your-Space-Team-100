package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing competitions in the database. Competitions have a start and end date, a theme
 * and hold a number of entries
 */
@Entity
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String theme;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @OneToMany(mappedBy = "competition", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompetitionDesign> entries;

    /**
     * JPA required no-args constructor
     */
    public Competition() {
    }

    public Competition(String theme, LocalDate startDate, LocalDate endDate) {
        this.theme = theme;
        this.startDate = startDate;
        this.endDate = endDate;
        this.entries = new ArrayList<>();
    }

    public List<CompetitionDesign> getEntries() {
        return entries;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public String getTheme() {
        return theme;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void addEntry(CompetitionDesign entry) {
        entries.add(entry);
    }
}
