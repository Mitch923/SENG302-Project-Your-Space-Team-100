package nz.ac.canterbury.seng302.homehelper;

import java.util.List;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.CompetitionService;
import nz.ac.canterbury.seng302.homehelper.service.DefaultDataService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Your Space web app entry-point Note @link{SpringBootApplication} annotation
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
// disable automatic security config in order for custom auth setup
public class HomeHelperApplication {

    RenovationService renovationService;
    UserService userService;

    @Autowired
    public HomeHelperApplication(RenovationService renovationService, UserService userService) {
        this.renovationService = renovationService;
        this.userService = userService;
    }

    /**
     * Main entry point, runs the Spring application
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(HomeHelperApplication.class, args);

    }

    /**
     * Runs commands as the application boots up
     */
    @Bean
    @ConditionalOnProperty(name = "app.runner.enabled", havingValue = "true")
    public CommandLineRunner commandLineRunner(ApplicationContext ctx,
            CompetitionService competitionService, DefaultDataService defaultDataService) {
        return (args) -> {
            int entries = 300;
            List<User> users = userService.createDefaultUsers(entries);

            renovationService.createDefaultRenovations(users);

            competitionService.createDefaultCompetitions(entries);

        };
    }

    @Bean
    @ConditionalOnProperty(name = "prod.generate", havingValue = "true")
    public CommandLineRunner generateProd(ApplicationContext ctx,
            CompetitionService competitionService, DefaultDataService defaultDataService) {
        return (args) -> {
            int entries = 300;
//            defaultDataService.generateUsers();
//            defaultDataService.createDefaultCompetitions(300);
            //defaultDataService.createDefaultCompetitionDesigns(entries);
//            defaultDataService.fixVoteCounts();

        };
    }

}
