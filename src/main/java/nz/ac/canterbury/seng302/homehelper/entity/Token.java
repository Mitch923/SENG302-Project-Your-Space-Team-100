package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generic Token class that can be used in any application where a unique token connectedto a user
 * is needed.
 */
@Entity
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    // Ensures only one token per user
    private User user;

    /**
     * JPA required no-args constructor
     */
    protected Token() {
    }

    public Token(User user) {
        int randomCode = ThreadLocalRandom.current().nextInt(0, 10000);
        String formattedCode = String.format("%04d", randomCode);
        String spacedCode = String.join(" ", formattedCode.split(""));
        this.token = spacedCode;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}


