package nz.ac.canterbury.seng302.homehelper.repository;

import jakarta.transaction.Transactional;
import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationDesign;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository extending Spring's CRURepository and PagingAndSortingRepository. Extendable by adding
 * new queries.
 */
@Repository
public interface RenovationDesignRepository extends JpaRepository<RenovationDesign, Long> {

    RenovationDesign getDesignById(long id);

    Page<RenovationDesign> findAllByRelatedRenovationRecord(RenovationRecord renovationRecord,
            Pageable pageable);

    @Query("SELECT d FROM RenovationDesign d WHERE d.relatedRenovationRecord.user.id = :userId")
    List<RenovationDesign> findAllByUserId(@Param("userId") Long userId);

    @Query(value = """
            WITH searchQuery AS (
                    SELECT d.id as id, ROW_NUMBER() OVER (ORDER BY d.id) as _index
                    FROM RenovationDesign d
                    WHERE (d.relatedRenovationRecord.id = :renovationRecordId)
            )
            SELECT s._index
            FROM searchQuery s
            WHERE s.id = :designId
            """)
    Integer findPageNumberByIdForRelatedRenovationRecord(Long renovationRecordId, Long designId);

    // Explicitly fetch the related rooms as hibernate didn't want load them when this was called.
    @Query("SELECT d FROM RenovationDesign d LEFT JOIN FETCH d.relatedRoom WHERE d.id = :designId AND d.relatedRenovationRecord = :renovationRecord")
    RenovationDesign findDesignByIdAndRenovation(@Param("designId") Long designId,
            @Param("renovationRecord") RenovationRecord renovationRecord);

    @Transactional
    @Modifying
    @Query(value = "UPDATE RenovationDesign d set d.iconName = :new_icon where d.id = :id")
    void updateDesignIconNameById(
            @Param("id") Long id, @Param("new_icon") String newIcon);

    @Query("SELECT d FROM RenovationDesign d WHERE d.relatedRenovationRecord.user.id = :userId AND LOWER(d.name) LIKE LOWER(:designName)")
    Page<RenovationDesign> searchDesignsByNameAndUserId(@Param("designName") String designName,
            @Param("userId") Long userId, Pageable pageable);

    Page<RenovationDesign> findByRelatedRenovationRecordUserAndNameContainingIgnoreCase(
            User user,
            String substring,
            Pageable pageable
    );
    Page<RenovationDesign> findByRelatedRenovationRecordUserAndRelatedRenovationRecordIdInAndNameContainingIgnoreCase(
            User user,
            List<Long> renovationRecordIds,
            String substring,
            Pageable pageable
    );

}