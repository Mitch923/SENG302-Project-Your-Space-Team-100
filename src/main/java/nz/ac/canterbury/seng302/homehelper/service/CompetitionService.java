package nz.ac.canterbury.seng302.homehelper.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionRepository;
import nz.ac.canterbury.seng302.homehelper.utils.DesignSortingType;
import nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * The service class for managing competitions
 */
@Service
@EnableScheduling
public class CompetitionService {

    private final CompetitionRepository competitionRepository;

    private final ThemeService themeService;

    private final UserService userService;
    private final CompetitionDesignRepository competitionDesignRepository;
    private final RenovationDesignService renovationDesignService;
    private final CompetitionDesignService competitionDesignService;

    Logger logger = LoggerFactory.getLogger(CompetitionService.class);

    @Autowired
    public CompetitionService(CompetitionRepository competitionRepository,
            ThemeService themeService, UserService userService,
            CompetitionDesignRepository competitionDesignRepository,
            RenovationDesignService renovationDesignService,
            CompetitionDesignService competitionDesignService) {
        this.competitionRepository = competitionRepository;
        this.themeService = themeService;
        this.userService = userService;
        this.competitionDesignRepository = competitionDesignRepository;
        this.renovationDesignService = renovationDesignService;
        this.competitionDesignService = competitionDesignService;
    }

    public Competition getCurrentCompetition() {
        return competitionRepository.getCurrentCompetition();
    }

    /**
     * Retrieves a competition based on its id
     *
     * @param id id of the requested competition
     * @return competition if it exists null otherwise
     */
    public Competition getCompetitionById(Long id) {
        return competitionRepository.getCompetitionById(id);
    }

    public List<Competition> getPreviousCompetitions() {
        return competitionRepository.getPreviousCompetitions();
    }

    public Competition save(Competition competition) {
        return competitionRepository.save(competition);
    }

    /**
     * Scheduled task that runs at 23:59 every sunday to create a new current competition
     */
    @Scheduled(cron = "${scheduler.cron}")
    public void scheduleNewCompetition() {
        createNewCompetition();
    }

    /**
     * Creates a new competition starting on the next monday and finishing on the next friday
     */
    public void createNewCompetition() {
        String newTheme = themeService.generateTheme();
        LocalDate today = LocalDate.now();
        LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate nextSunday = today.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        Competition competition = new Competition(newTheme, nextMonday, nextSunday);
        competitionRepository.save(competition);
    }

    /**
     * Create the default data for competitions. Create 1 current and 5 past competitions.
     *
     * @param totalEntries number of entries to make on the current competition (purely for
     *                     debugging)
     */
    public void createDefaultCompetitions(int totalEntries) {
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 100; i++) {
            LocalDate monday = today
                    .minusWeeks(i)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            LocalDate sunday = monday.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            String theme = themeService.generateTheme();
            this.save(new Competition(theme, monday, sunday));
        }

        Competition competition = getCurrentCompetition();
        Competition lastCompetition = getCompetitionById(2L);
        CompetitionDesign johnsEntrySubmitted = new CompetitionDesign("John's Submitted Design",
                "Wow!", "", lastCompetition,
                userService.getUserByEmail("john@example.com").orElse(null));
        johnsEntrySubmitted.setSubmitted(true);
        lastCompetition.addEntry(johnsEntrySubmitted);

        for (long i = 4L; i < totalEntries + 4;
                i++) { // 4L means that an entry will not be created for john/jane/etc...
            User user = userService.getUserById(i).orElse(null);
            competition.addEntry(
                    new CompetitionDesign("Design Designer" + i,
                            "Wow!", "", competition, userService.getUserById(i).orElse(null))
            );
            lastCompetition.addEntry(
                    new CompetitionDesign("Design Designer" + i,
                            "Wow!", "", lastCompetition,
                            userService.getUserById(i).orElse(null))
            );
        }
        lastCompetition = this.save(lastCompetition);
        for (CompetitionDesign design : lastCompetition.getEntries()) {
            design.setSceneChunkDirectory(String.format("competition_design_id%s", design.getId()));
            design.setThumbnailFilePath(
                    UploadDirectory.COMPETITION_THUMBNAILS.getRelativePathForDB().resolve(
                                    String.format("competition_design_image_id%s.jpeg", design.getId()))
                            .toString());
            design.setSubmitted(true);
        }
        this.save(lastCompetition);

        Competition comp = this.save(competition);
        for (CompetitionDesign design : comp.getEntries()) {
            design.setSceneChunkDirectory(String.format("competition_design_id%s", design.getId()));
            design.setThumbnailFilePath(
                    UploadDirectory.COMPETITION_THUMBNAILS.getRelativePathForDB().resolve(
                                    String.format("competition_design_image_id%s.jpeg", design.getId()))
                            .toString());
            design.setSubmitted(true);
        }

//      Give jane an unsubmitted design in a past competition to check it does not appear.
        CompetitionDesign janesEntryUnsubmitted = new CompetitionDesign("Jane's Unsubmitted Design",
                "Wow!", "", lastCompetition,
                userService.getUserByEmail("jane@example.com").orElse(null));
        janesEntryUnsubmitted.setSubmitted(false);
        lastCompetition.addEntry(janesEntryUnsubmitted);
        this.save(lastCompetition);

        this.save(comp);

        int maxVotes = 100;
        int step = 2;

        List<CompetitionDesign> lastCompetitionEntries = new ArrayList<>(
                lastCompetition.getEntries());

        for (int i = 0; i < lastCompetitionEntries.size(); i++) {
            CompetitionDesign design = lastCompetitionEntries.get(i);

            int numVotes = Math.max(0, maxVotes - (i * step));

            for (int v = 0; v < numVotes; v++) {
                long voterId = 4L + (v % totalEntries);
                User voter = userService.getUserById(voterId).orElse(null);

                if (voter != null && !voter.equals(design.getUser()) && !design.getVotedUsers()
                        .contains(voter)) {
                    design.getVotedUsers().add(voter);
                    design.setVoteCount(design.getVoteCount() + 1);
                }
            }
        }
        this.save(lastCompetition);

        List<CompetitionDesign> currentEntries = new ArrayList<>(comp.getEntries());
        for (int i = 0; i < Math.min(50, currentEntries.size()); i++) {
            CompetitionDesign design = currentEntries.get(i);

            int numVotes = Math.max(0, maxVotes - (i * step));

            for (int v = 0; v < numVotes; v++) {
                long voterId = 4L + v;
                User voter = userService.getUserById(voterId).orElse(null);

                if (voter != null && !voter.equals(design.getUser()) && !design.getVotedUsers()
                        .contains(voter)) {
                    design.getVotedUsers().add(voter);
                    design.setVoteCount(design.getVoteCount() + 1);
                }
            }
        }
        this.save(comp);
    }

    /**
     * Creates a new competition entry for the current competition and the current logged-in user.
     * Defaults the name of the entry to {First Name}'s Design. Saves the design in the DB and
     * returns the design with its repository assigned id.
     *
     * @return {@code CompetitionDesign} new competition design
     */
    public CompetitionDesign createNewCompetitionEntry() {
        User loggedInUser = userService.getLoggedUser();
        Competition currentCompetition = getCurrentCompetition();

        if (userAlreadyEnteredInCurrentCompetition()) {
            logger.info("User already has an entry for competition id {}",
                    currentCompetition.getId());
            return null;
        }
        logger.info("Creating new competition entry for competition id {}",
                currentCompetition.getId());
        CompetitionDesign competitionDesign = new CompetitionDesign(
                loggedInUser.getFirstName() + "'s Design", "", "", currentCompetition,
                loggedInUser);
        return saveCompetitionEntry(competitionDesign);
    }

    /**
     * Gets the currently logged-in users competition design.
     *
     * @return A Competition design object or null
     */
    public CompetitionDesign getCurrentUserCompetitionDesign() {
        User loggedInUser = userService.getLoggedUser();
        Competition currentCompetition = getCurrentCompetition();
        return competitionDesignRepository.getByCompetitionIdAndUserId(
                currentCompetition.getId(), loggedInUser.getId());
    }

    private CompetitionDesign saveCompetitionEntry(CompetitionDesign entry) {
        return competitionDesignRepository.save(entry);
    }

    /**
     * Returns the truth value of the logged-in user already having an entry in the current weekly
     * competition.
     *
     * @return whether the user is entered in the current competition
     */
    private boolean userAlreadyEnteredInCurrentCompetition() {
        User loggedInUser = userService.getLoggedUser();
        Competition currentCompetition = getCurrentCompetition();
        CompetitionDesign existingEntry = competitionDesignRepository.getByCompetitionIdAndUserId(
                currentCompetition.getId(), loggedInUser.getId());
        return existingEntry != null;
    }

    /**
     * Gets a specified page of designs for the given competition
     *
     * @param competitionId competition to get results for
     * @param pageNum       page index
     * @param pageSize      number of results per page
     * @param sortingType   the type of sorting that should be applied when retrieving the page
     * @return Pageable object that contains results
     */
    public Page<CompetitionDesign> getCompetitionDesignsPage(
            Long competitionId,
            int pageNum,
            int pageSize,
            DesignSortingType sortingType
    ) {
        Pageable pageable = PageRequest.of(pageNum, pageSize, sortingType.getSort().and(
                Sort.by("id").ascending()));
        return competitionDesignRepository.getPageOfCompetitionDesignsByCompetitionIdAndSubmitted(
                competitionId, true, pageable);
    }

    /**
     * Returns true if it is valid that the user can import the design with the given id. This is
     * valid if the user isn't already entered in the current competition and if the user owns the
     * design with the given id.
     *
     * @param id of the renovation design to import
     * @return true if the user can import the design to the competition, false otherwise
     */
    public boolean validateImportDesign(long id) {
        if (userAlreadyEnteredInCurrentCompetition()) {
            return false;
        }
        return renovationDesignService.userOwnsRenovationDesign(id);
    }


    /**
     * Toggles the state of the logged-in user having voted for the given competition design
     *
     * @param competitionDesignId id of the competition design whose number of votes is changed
     */
    public void toggleVote(Long competitionDesignId) {
        CompetitionDesign competitionDesign = competitionDesignService.getCompetitionDesignById(
                competitionDesignId);
        User user = userService.getLoggedUser();
        if (!user.equals(competitionDesign.getUser())) {
            if (user.getVotedOnDesigns().contains(competitionDesign)) {
                competitionDesign.decrementVotes(user);
            } else {
                competitionDesign.incrementVotes(user);
            }
            saveCompetitionEntry(competitionDesign);
        }
    }

    /**
     * Returns true if the current logged-in user doesn't own the competition design with the given
     * id and the competition is open.
     *
     * @param competitionDesignId id of the competition design to validate against
     * @return whether changing your vote is valid
     */
    public boolean validateToggleVote(Long competitionDesignId) {
        CompetitionDesign competitionDesign = competitionDesignService.getCompetitionDesignById(
                competitionDesignId);
        if (competitionDesignService.userOwnsCompetitionDesign(competitionDesignId)) {
            logger.info("Cannot toggle vote for entry id {} as they own the entry",
                    competitionDesignId);
            return false;
        }
        if (!Objects.equals(getCurrentCompetition().getId(),
                competitionDesign.getCompetition().getId())) {
            logger.info(
                    "Cannot toggle vote for entry id {} as it is not entered in the current competition",
                    competitionDesignId);
            return false;
        }
        if (!competitionDesign.isSubmitted()) {
            logger.info("Cannot toggle vote for entry id {} as it is not submitted for voting",
                    competitionDesignId);
            return false;
        }
        return true;
    }

    public CompetitionDesign getCompetitionDesignByCompetitionAndUser(Long competitionId) {
        User loggedInUser = userService.getLoggedUser();
        return competitionDesignRepository.getByCompetitionIdAndUserId(competitionId,
                loggedInUser.getId());
    }

    public Integer getSubmittedEntriesCount(Long competitionId) {
        Competition comp = getCompetitionById(competitionId);
        return comp.getEntries().stream().filter(CompetitionDesign::isSubmitted).toList().size();
    }

    /**
     * Returns the top voted competition entries from the competition with the given id. The Array
     * is padded with null if there are less than requested number of entries.
     *
     * @param competitionId of the competition to retrieve from
     * @param numResults    num of results to retrieve
     * @return an Array containing the top voted entries
     */
    public CompetitionDesign[] getTopCompetitionDesignsByCompetition(Long competitionId,
            int numResults) {
        List<CompetitionDesign> top3CompetitionDesigns = competitionDesignRepository.findTopByCompetitionOrderByVotesDesc(
                competitionId,
                Pageable.ofSize(numResults));
        return top3CompetitionDesigns.toArray(new CompetitionDesign[numResults]);
    }

    /**
     * Gets a specified page of previous competitions
     *
     * @param pageNum  page index
     * @param pageSize number of results per page
     * @return Pageable object that contains results
     */
    public Page<Competition> getPageOfPreviousCompetitions(int pageNum,
            int pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        return competitionRepository.getPageOfPreviousCompetitions(pageable);
    }

    /**
     * @param id the id of the competition entry you want to check exists
     * @return true if it exists, false if not
     */
    public boolean entryExists(Long id) {
        return competitionDesignRepository.getById(id) != null;
    }
}
