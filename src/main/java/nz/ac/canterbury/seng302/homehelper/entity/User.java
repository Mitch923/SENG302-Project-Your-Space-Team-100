package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Entity class reflecting a user's account
 */
@Entity
@Table(name = "APP_USER")
public class User {

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<RenovationRecord> renovationRecords = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private final List<String> userRoles = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // used as username for authentication

    @Column(nullable = false, length = 10000)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column()
    private String lastName;

    @Column
    private LocalDateTime createdTimestamp;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Token token;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    @Column(name = "profile_image_type")
    private String profileImageFileType;

    @Column
    private String resetPasswordToken;

    @Column
    private String failSubmissionImagePath;

    @Column
    private String failSubmissionImageOriginalName;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "user_location",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "location_id", referencedColumnName = "id")})
    private Location userLocation;

    @ManyToMany(mappedBy = "votedUsers")
    private List<CompetitionDesign> votedOnDesigns;

    /**
     * JPA required no-args constructor
     */
    protected User() {
    }

    /**
     * Creates a new User object
     *
     * @param email     email address of user
     * @param password  password of user
     * @param firstName first name of user
     * @param lastName  last name of user
     */
    public User(String email, String password, String firstName, String lastName) {
        this.email = email.toLowerCase();
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdTimestamp = LocalDateTime.now();
        this.userRoles.add("ROLE_UNVERIFIED");
        this.votedOnDesigns = new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        createdTimestamp = LocalDateTime.now();
    }

    public void grantAuthority(String authority) {
        this.userRoles.add(authority);
    }

    public void revokeAuthority(String authority) {
        this.userRoles.remove(authority);
    }

    /**
     * Convert list of roles to authorites
     *
     * @return List of SimpleGrantedAuthorities
     */
    public List<? extends GrantedAuthority> getAuthorities() {
        return userRoles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        if (!Objects.equals(lastName, "")) {
            return this.firstName + " " + this.lastName;
        }
        return this.firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }

    // Adapted from ChatGPT prompt:
    // "How do I convert a LocalDateTime to a Date in format dd/mm/yyyy"
    public String getCreatedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return this.createdTimestamp.format(formatter);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public String getProfileImageFileType() {
        return this.profileImageFileType;
    }

    public void setProfileImageFileType(String fileType) {
        this.profileImageFileType = fileType;
    }

    public String getProfileImagePath() {
        return this.profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public String getFailSubmissionImagePath() {
        return failSubmissionImagePath;
    }

    public void setFailSubmissionImagePath(String failSubmissionImagePath) {
        this.failSubmissionImagePath = failSubmissionImagePath;
    }

    public String getFailSubmissionImageOriginalName() {
        return failSubmissionImageOriginalName;
    }

    public void setFailSubmissionImageOriginalName(String failSubmissionImageOriginalName) {
        this.failSubmissionImageOriginalName = failSubmissionImageOriginalName;
    }

    public Location getUserLocation() {
        return this.userLocation;
    }

    public void setUserLocation(Location location) {
        this.userLocation = location;
    }

    public List<CompetitionDesign> getVotedOnDesigns() {
        return votedOnDesigns;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) {
            return false;
        }
        User otherUser = (User) o;
        return otherUser.getId().equals(this.id) &&
                otherUser.getFirstName().equals(this.firstName) &&
                otherUser.getEmail().equals(this.email);
    }
}
