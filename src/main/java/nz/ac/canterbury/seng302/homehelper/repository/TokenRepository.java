package nz.ac.canterbury.seng302.homehelper.repository;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.Token;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TokenRepository extends CrudRepository<Token, Long> {

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM token WHERE user_id = :user_id", nativeQuery = true)
    void deleteByUserId(@Param("user_id") Long user_id);

    @Query(value = "SELECT * FROM token t WHERE t.user_id = :user_id", nativeQuery = true)
    Token getByUserId(@Param("user_id") Long user_id);
}
