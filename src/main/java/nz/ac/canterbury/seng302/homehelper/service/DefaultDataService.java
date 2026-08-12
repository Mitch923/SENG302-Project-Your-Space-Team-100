package nz.ac.canterbury.seng302.homehelper.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import nz.ac.canterbury.seng302.homehelper.entity.Competition;
import nz.ac.canterbury.seng302.homehelper.entity.CompetitionDesign;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionDesignRepository;
import nz.ac.canterbury.seng302.homehelper.repository.CompetitionRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.utils.FileUtilities;
import nz.ac.canterbury.seng302.homehelper.utils.JarResourceFileLoader;
import nz.ac.canterbury.seng302.homehelper.utils.UploadDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DefaultDataService {

    private static final String USERNAME_PATH = "/userNames.txt";
    private static final String DESIGN_NAME_PATH = "/designNames.txt";
    private static final String PFP_DIR = "/defaultPfps/";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final UserService userService;
    private final ThemeService themeService;
    private final CompetitionService competitionService;
    private final CompetitionRepository competitionRepository;
    private final CompetitionDesignRepository competitionDesignRepository;
    private final JarResourceFileLoader jarResourceFileLoader;
    private final Path testUserImageDir = Paths.get(
            "static",
            "utils"
    );
    Logger logger = LoggerFactory.getLogger(DefaultDataService.class);


    @Value("${static.resource.folder}")
    private String uploadsFolder;

    @Autowired
    public DefaultDataService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenRepository tokenRepository,
            JarResourceFileLoader jarResourceFileLoader,
            ThemeService themeService,
            UserService userService,
            CompetitionService competitionService,
            CompetitionRepository competitionRepository,
            CompetitionDesignRepository competitionDesignRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.jarResourceFileLoader = jarResourceFileLoader;
        this.themeService = themeService;
        this.userService = userService;
        this.competitionService = competitionService;
        this.competitionRepository = competitionRepository;
        this.competitionDesignRepository = competitionDesignRepository;
    }


    public void fixVoteCounts() {
        for (CompetitionDesign competitionDesign : competitionDesignRepository.findAll()) {
            competitionDesign.setVoteCount(competitionDesign.getVotedUsers().size());
        }
    }

    public void generateUsers() {
        String password = passwordEncoder.encode("P4$$word");
        User john = new User("john@example.com", password, "John", "Doe");
        User jane = new User("jane@example.com", password, "Jane", "Doe");
        User sarah = new User("sarahandjackthompson@gmail.com", password, "Sarah", "Thompson");
        john.revokeAuthority("ROLE_UNVERIFIED");
        john.grantAuthority("ROLE_USER");
        john = userRepository.save(john); // Ensure john is updated with new id

        jane.revokeAuthority("ROLE_UNVERIFIED");
        jane.grantAuthority("ROLE_USER");
        jane = userRepository.save(jane);

        sarah.revokeAuthority("ROLE_UNVERIFIED");
        sarah.grantAuthority("ROLE_USER");
        sarah = userRepository.save(sarah);

        int totalExtraUsers = 300;

        List<String> userNames = FileUtilities.readWordsFromResource(USERNAME_PATH);
        for (int i = 0; i < totalExtraUsers; i++) {
            String name = userNames.get(i % userNames.size());
            String capitalized = (name == null || name.isEmpty())
                    ? name
                    : name.substring(0, 1).toUpperCase() + name.substring(1);
            User user = new User("generic" + i + "@example.com", password,
                    capitalized,
                    "");
            user.revokeAuthority("ROLE_UNVERIFIED");
            user.grantAuthority("ROLE_USER");
            userRepository.save(user);
        }
    }

    public void createDefaultCompetitions(int totalEntries) {
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 100; i++) {
            LocalDate monday = today
                    .minusWeeks(i)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            LocalDate sunday = monday.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

            String theme = themeService.generateTheme();
            competitionRepository.save(new Competition(theme, monday, sunday));
        }
    }

    public void createDefaultCompetitionDesigns(int totalEntries) {
        List<User> allUsers = userRepository.findAll();
        List<String> designNames = FileUtilities.readWordsFromResource(DESIGN_NAME_PATH);

        // === Get Competitions ===
        Competition currentCompetition = competitionService.getCurrentCompetition();
        Competition lastCompetition = competitionService.getCompetitionById(2L);
        List<Competition> allCompetitions = competitionRepository.findAll();

        if (allCompetitions.stream().noneMatch(c -> c.getId().equals(currentCompetition.getId()))) {
            allCompetitions.add(currentCompetition);
        }

        allCompetitions = allCompetitions.stream()
                .sorted(Comparator.comparingLong(Competition::getId))
                .collect(Collectors.toList());

        // === Create special entries for John and Jane (optional: keep or remove) ===
        // createSpecialEntries(allUsers, lastCompetition);  // You can remove this if you don't want them

        // === Filter eligible users ===
        List<User> eligibleUsers = allUsers.stream()
                .filter(user -> user.getId() > 22)
                .collect(Collectors.toList());

        int userIndex = 0;

        for (Competition competition : allCompetitions) {
            logger.info("Processing competition {} (ID: {})", competition.getTheme(),
                    competition.getId());

            int entriesThisComp = competition.getId().equals(currentCompetition.getId())
                    ? 80 + (int) (Math.random() * 20) // 80–99 entries
                    : 20 + (int) (Math.random() * 16); // 20–35 entries

            for (int i = 0; i < entriesThisComp; i++) {
                User user = eligibleUsers.get(userIndex % eligibleUsers.size());
                userIndex++;

                String designName = designNames.get((userIndex + i) % designNames.size());

                CompetitionDesign design = new CompetitionDesign(designName, "", null, competition,
                        user);
                design.setSubmitted(true);

                // Save immediately to get ID (if needed for setting file paths)
                design = competitionDesignRepository.save(design);

                // Set file paths based on design ID
                design.setSceneChunkDirectory("competition_design_id" + design.getId());
                design.setThumbnailFilePath(
                        UploadDirectory.COMPETITION_THUMBNAILS.getRelativePathForDB()
                                .resolve("competition_design_image_id" + design.getId() + ".jpeg")
                                .toString()
                );

                // Save updated paths
                competitionDesignRepository.save(design);
            }
        }

        logger.info("Finished creating competition designs");
    }


    private void createSpecialEntries(List<User> allUsers, Competition lastCompetition) {
        // === John's Submitted Entry ===
        User john = userService.getUserByEmail("john@example.com").orElseThrow(() ->
                new IllegalStateException("John user not found"));

        CompetitionDesign johnsEntry = new CompetitionDesign(
                "John's Submitted Design", "Wow!", "", lastCompetition, john);
        johnsEntry.setSubmitted(true);
        johnsEntry = competitionDesignRepository.save(johnsEntry);

        johnsEntry.setSceneChunkDirectory("competition_design_id" + johnsEntry.getId());
        johnsEntry.setThumbnailFilePath(
                UploadDirectory.COMPETITION_THUMBNAILS.getRelativePathForDB().resolve(
                        "competition_design_image_id" + johnsEntry.getId() + ".jpeg"
                ).toString()
        );

        // Add votes efficiently
        List<User> johnVoters = allUsers.stream()
                .filter(user -> user.getId() >= 23 && !user.getId().equals(john.getId()))
                .limit(260)
                .collect(Collectors.toList());

        for (User voter : johnVoters) {
            johnsEntry.incrementVotes(voter);
        }
        competitionDesignRepository.save(johnsEntry);

        // === Jane's Unsubmitted Entry ===
        User jane = userService.getUserByEmail("jane@example.com").orElseThrow(() ->
                new IllegalStateException("Jane user not found"));

        CompetitionDesign janesEntry = new CompetitionDesign(
                "Jane's Unsubmitted Design", "Wow!", "", lastCompetition, jane);
        janesEntry.setSubmitted(false);
        janesEntry.setSceneChunkDirectory("competition_design_id" + janesEntry.getId());
        janesEntry.setThumbnailFilePath(
                UploadDirectory.COMPETITION_THUMBNAILS.getRelativePathForDB().resolve(
                        "competition_design_image_id" + janesEntry.getId() + ".jpeg"
                ).toString()
        );
        competitionDesignRepository.save(janesEntry);
    }

    private void saveBatchAndSetProperties(List<CompetitionDesign> batch,
            List<User> eligibleUsers) {
        // Save all designs first to get IDs
        List<CompetitionDesign> savedDesigns = (List<CompetitionDesign>) competitionDesignRepository.saveAll(
                batch);

        // Now set ID-dependent properties and add votes
        for (CompetitionDesign entry : savedDesigns) {
            entry.setSceneChunkDirectory("competition_design_id" + entry.getId());
            entry.setThumbnailFilePath(
                    UploadDirectory.COMPETITION_THUMBNAILS.getRelativePathForDB().resolve(
                            "competition_design_image_id" + entry.getId() + ".jpeg"
                    ).toString()
            );
        }

        // Final save with all properties and votes
        competitionDesignRepository.saveAll(savedDesigns);
    }

    private void addVotesToDesign(CompetitionDesign entry, List<User> eligibleUsers) {
        // Filter out the design owner
        List<User> availableVoters = eligibleUsers.stream()
                .filter(u -> !u.getId().equals(entry.getUser().getId()))
                .collect(Collectors.toList());

        int minVotes = (entry.getId() < 7) ? 80 : 10;
        int maxVotes = Math.min(250, availableVoters.size()); // Don't exceed available voters
        int totalVotes = minVotes + (int) (Math.random() * (maxVotes - minVotes + 1));

        // Shuffle once and take the first N voters
        Collections.shuffle(availableVoters);

        for (int i = 0; i < Math.min(totalVotes, availableVoters.size()); i++) {
            entry.incrementVotes(availableVoters.get(i));
        }
    }


}
