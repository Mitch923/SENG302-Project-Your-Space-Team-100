package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.UniqueConstraint;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * A design used for design in competitions. Related to competitions instead of renovations and
 * rooms and has the option to be submitted.
 */
@Entity
public class CompetitionDesign {

    private final boolean isRenovationDesign = false;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 512)
    private String name;
    @Column(nullable = false, length = 1024)
    private String description;
    @Column
    private String thumbnailFilePath;
    @Column(name = "vote_count", nullable = false)
    private Integer voteCount = 0;
    @Column(nullable = false)
    private boolean submitted;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne
    @JoinColumn(name = "competition_id", nullable = false)
    private Competition competition;
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_design_vote",
            joinColumns = @JoinColumn(name = "competition_design_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"competition_design_id",
                    "user_id"}))
    private Set<User> votedUsers = new HashSet<>();

    @Column
    private String sceneChunkDirectory;

    @Column
    private Integer chunkCount;

    public CompetitionDesign() {
    }

    public CompetitionDesign(String name, String description,
            String thumbnailFilePath, Competition competition, User user) {
        this.name = name;
        this.description = description;
        this.thumbnailFilePath = thumbnailFilePath;
        this.competition = competition;
        this.submitted = false;
        this.user = user;
        this.votedUsers = new HashSet<>();
        this.voteCount = 0;
        this.sceneChunkDirectory = "";
    }

    public String getSceneChunkDirectory() {
        return sceneChunkDirectory;
    }

    public void setSceneChunkDirectory(String sceneChunkDirectory) {
        this.sceneChunkDirectory = sceneChunkDirectory;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
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

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public String getThumbnailFilePath() {
        return thumbnailFilePath;
    }

    public void setThumbnailFilePath(String thumbnailFilePath) {
        this.thumbnailFilePath = thumbnailFilePath;
    }

    public String getThumbnailFilename() {
        if (thumbnailFilePath != null) {
            return Paths.get(thumbnailFilePath).getFileName().toString();
        } else {
            return null;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRenovationDesign() {
        return isRenovationDesign;
    }

    public int getNumberOfVotes() {
        return voteCount;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public void incrementVotes(User user) {
        if (!this.votedUsers.contains(user)) {
            this.votedUsers.add(user);
            this.voteCount++;
            user.getVotedOnDesigns().add(this);
        }
    }

    public void decrementVotes(User user) {
        if (this.votedUsers.remove(user)) {
            this.voteCount--;
            user.getVotedOnDesigns().remove(this);
        }
    }

    public Set<User> getVotedUsers() {
        return votedUsers;
    }
}