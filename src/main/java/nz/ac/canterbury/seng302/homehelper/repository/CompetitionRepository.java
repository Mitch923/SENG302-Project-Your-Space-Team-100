package nz.ac.canterbury.seng302.homehelper.repository;

import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    @Query("SELECT c FROM Competition c WHERE c.endDate >= current date ORDER BY c.endDate desc limit 1")
    Competition getCurrentCompetition();

    @Query("SELECT c FROM Competition c WHERE c.endDate < current date ORDER BY c.endDate desc")
    List<Competition> getPreviousCompetitions();

    Competition getCompetitionById(Long id);

    @Query("SELECT c FROM Competition c WHERE c.endDate < CURRENT_DATE ORDER BY c.endDate DESC")
    Page<Competition> getPageOfPreviousCompetitions(Pageable pageable);
}
