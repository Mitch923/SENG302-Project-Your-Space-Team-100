package nz.ac.canterbury.seng302.homehelper.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import nz.ac.canterbury.seng302.homehelper.entity.Token;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.TokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.concurrent.DelegatingSecurityContextScheduledExecutorService;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Schedules deletion tasks for users who have registered and need to be verified.<br> Stores a Map
 * of future tasks that can be cancelled if the user registers in time.
 */
@Service
public class EmailVerificationService {

    private final DelegatingSecurityContextScheduledExecutorService executorService =
            new DelegatingSecurityContextScheduledExecutorService(
                    Executors.newScheduledThreadPool(4));

    // Concurrent Map to ensure thread safety
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledRegisterTasks = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledPasswordResetTasks = new ConcurrentHashMap<>();

    private final UserRepository userRepository;

    private final TokenRepository tokenRepository;

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);
    @Value("${email.verification.deletionTime}")
    private int deletionTimeRegister;
    @Value("${email.resetPassword.deletionTime}")
    private int deletionTimeResetPassword;

    @Autowired
    public EmailVerificationService(UserRepository userRepository,
            TokenRepository tokenRepository, UserService userService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.userService = userService;
    }

    /**
     * Schedules a user for deletion from the database in 10 minutes.<br> Also adds the scheduled
     * future to the Concurrent hashmap for possible canceling.
     *
     * @param user Target user for deletion
     */
    public void scheduleUserDeletion(User user) {
        logger.info("Scheduling user deletion: {}", user.getEmail());

        if (!scheduledRegisterTasks.containsKey(user.getId())) {

            Runnable task = () -> {
                try {
                    // Log user out
                    if (userService.isLoggedIn() && userService.getLoggedUser().getId()
                            .equals(user.getId())) {
                        logger.info("Logging Out user: {}", user.getEmail());
                        // Set new Anonymous user
                        SecurityContextHolder.getContext()
                                .setAuthentication(new AnonymousAuthenticationToken(
                                        "key", "anonymousUser",
                                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
                    }
                    logger.info(
                            "Logged out ?? (from emailverification) = {}",
                            userService.isLoggedIn());
                    logger.info("Deleting unverified user: {}", user.getEmail());
                    userRepository.delete(user);
                    tokenRepository.deleteByUserId(user.getId());
                } catch (Exception e) {
                    logger.error("Error deleting unverified user: {}", user.getEmail(), e);
                } finally {
                    scheduledRegisterTasks.remove(user.getId());
                }
            };

            ScheduledFuture<?> future = executorService.schedule(task, deletionTimeRegister,
                    TimeUnit.SECONDS); // 10 minute delay
            // Update tasks with new future
            scheduledRegisterTasks.put(user.getId(), future);
        }
    }

    /**
     * Schedules the revocation of the validity of the given reset password token to happen after 10
     * minutes
     *
     * @param token String token whose validity to revoke
     */
    public void scheduleResetPasswordTokenRevocation(String token) {
        User userWhoseToken = userService.getUserByResetPasswordToken(token).get();
        logger.info("Scheduling reset password token deletion: {} for user: {}", token,
                userWhoseToken.getEmail());

        Runnable task = () -> {
            try {
                userService.revokeResetPasswordToken(token);
                logger.info("Reset password token deletion: {}", token);
            } catch (Exception e) {
                logger.error("Error deleting reset password token: {}", token, e);
            } finally {
                scheduledPasswordResetTasks.remove(userWhoseToken.getId());
            }
        };

        ScheduledFuture<?> future = executorService.schedule(task, deletionTimeResetPassword,
                TimeUnit.SECONDS);
        this.scheduledPasswordResetTasks.put(userWhoseToken.getId(), future);
    }

    /**
     * Cancels any deletion task associated with the provided user <br>
     *
     * @param user the user targeted for deletion
     * @return True if task was canceled, false if task doesn't exist or already executed.
     */
    public boolean cancelUserDeletion(User user) {
        // Cancel and remove task
        ScheduledFuture<?> future = scheduledRegisterTasks.remove(user.getId());

        if (future != null) { // Ensure task hasn't already executed
            logger.info("Cancelling user deletion: {}", user.getEmail());
            return future.cancel(false);
        }

        return false;
    }

    /**
     * Validates a token
     *
     * @param tokenString the provided token (in the form: 1234)
     * @param id          the user id the target token is assigned
     * @return True if the token matches the one in the database, false if not.
     */
    public boolean ensureValidCode(String tokenString, Long id) {
        Token token = tokenRepository.getByUserId(id);
        if (token == null) {
            return false;
        }

        String spacedCode = String.join(" ", tokenString.split(""));

        return token.getToken().equals(spacedCode);
    }

    public ConcurrentHashMap<Long, ScheduledFuture<?>> getScheduledRegisterTasks() {
        return scheduledRegisterTasks;
    }

    public ConcurrentHashMap<Long, ScheduledFuture<?>> getScheduledPasswordResetTasks() {
        return scheduledPasswordResetTasks;
    }
}
