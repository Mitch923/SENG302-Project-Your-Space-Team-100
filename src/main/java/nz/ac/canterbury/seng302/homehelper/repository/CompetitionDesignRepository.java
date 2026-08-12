package nz.ac.canterbury.seng302.homehelper.repository;

import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompetitionDesignRepository extends CrudRepository<CompetitionDesign, Long> {

    CompetitionDesign getById(Long competitionId);

    CompetitionDesign getByCompetitionIdAndUserId(Long competitionId, Long userId);

    Page<CompetitionDesign> getPageOfCompetitionDesignsByCompetitionIdAndSubmitted(
            Long competitionId, boolean submitted, Pageable pageable);

    /**
     * ChatGPT generated JPQL query. Finds the top voted competition designs for a given
     * competition
     *
     * @param competitionId id of the competition that the entries should be from
     * @param pageable      object to indicate how many entries to return
     * @return a List containing the top voted competition entries from the requested competition
     */
    @Query("""
            SELECT d
            FROM CompetitionDesign d
            LEFT JOIN d.votedUsers v
            WHERE d.competition.id = :competitionId AND d.submitted = true
            GROUP BY d
            ORDER BY COUNT(v) DESC, d.id ASC
            """)
    List<CompetitionDesign> findTopByCompetitionOrderByVotesDesc(
            @Param("competitionId") Long competitionId,
            Pageable pageable);
}
