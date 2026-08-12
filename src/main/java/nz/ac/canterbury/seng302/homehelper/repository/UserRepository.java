package nz.ac.canterbury.seng302.homehelper.repository;

import java.util.List;
import java.util.Optional;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository accessory extending Spring's CRUDrepository. Extendable by adding new queries.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    Optional<User> findByResetPasswordToken(String resetPasswordToken);

    List<User> findAll();

    /**
     * Updates a target user (identified by oldId) with the details of newUser<br> *DOES NOT UPDATE
     * PASSWORD*
     *
     * @param newUser newUser details
     * @param oldId   target id
     */
    @Modifying
    @Query(
            "UPDATE User u SET u.firstName = :#{#newUser.firstName}, u.lastName = :#{#newUser.lastName}, u.email = :#{#newUser.email} "
                    +
                    "WHERE u.id = :oldId")
    void updateUser(@Param("newUser") User newUser, @Param("oldId") Long oldId);

    @Modifying
    @Query("UPDATE User u SET u.firstName = :firstName, u.lastName = :lastName, u.email = :email " +
            "WHERE u.id = :oldId")
    void updateUser(@Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("oldId") Long oldId);

    void flush();
}
