package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.utils.JarResourceFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Handles all the email creation and sending logic
 */
@Service
@EnableScheduling
public class SpringEmailService {

    private static final Logger logger = LoggerFactory.getLogger(SpringEmailService.class);
    private static final String LOGO_PATH = "static/img/logo/hh-logo-500.png";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UserService userService;
    private final JarResourceFileLoader jarResourceFileLoader;

    @Value("${email.resetPassword.baseUrl}")
    private String baseUrl;

    @Autowired
    public SpringEmailService(
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            UserService userService,
            JarResourceFileLoader jarResourceFileLoader
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.userService = userService;
        this.jarResourceFileLoader = jarResourceFileLoader;
    }

    /**
     * Sends an email using the provided parameters.
     *
     * @param toEmail   recipient email address
     * @param subject   subject of the email
     * @param template  Thymeleaf template name
     * @param variables variables to be injected into the template
     */
    @Async
    protected void sendEmail(String toEmail, String subject, String template, Context variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String htmlContent = templateEngine.process(template, variables);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // File logoFile = new File(LOGO_PATH);
            File logoFile = jarResourceFileLoader.getResourceAsTempFile(LOGO_PATH);
            helper.addInline("hhLogo", logoFile);

            mailSender.send(message);
        } catch (MessagingException e) {
            logger.warn("Failed to send email: {}", e.getMessage());
        } catch (IOException e) {
            logger.warn("Failed to send email, file not found: {}", e.getMessage());
        }
    }

    /**
     * Sends an email with a verification code upon sign-up.
     *
     * @param firstName user's first name
     * @param toEmail   recipient's email address
     * @param code      verification code
     */
    @Async
    public void sendSignUpEmail(String firstName, String toEmail, String code) {
        Context context = new Context();
        context.setVariable("name", firstName);
        context.setVariable("message", code);

        sendEmail(toEmail, "Your Space Verification Code", "signUpEmailTemplate", context);
    }

    /**
     * Sends an email notification when the user changes their password.
     *
     * @param firstName user's first name
     * @param toEmail   recipient's email address
     */
    @Async
    public void sendPasswordChangeEmail(String firstName, String toEmail) {
        Context context = new Context();
        context.setVariable("name", firstName);

        sendEmail(toEmail, "Your Password Was Recently Changed", "passwordChangeNotificationEmail",
                context);
    }

    /**
     * Uses the spring mail dependency to send a password reset email to the given email address if,
     * and only if the email is connected to a registered user.
     *
     * @param email String representation of the email to check and send to
     */
    @Async
    public void sendResetPasswordEmail(String email) {
        if (!userService.existsByEmail(email)) {
            logger.info("User with email {} does not exist, no email sent", email);
            return; // Don't send the email
        }

        User userToSend = userService.getUserByEmail(email).get();
        String resetPasswordToken = userToSend.getResetPasswordToken();

        Context context = new Context();
        context.setVariable("name", userToSend.getFirstName());
        context.setVariable("resetLink", baseUrl + "/reset?token=" + resetPasswordToken);
        logger.info("reset link is {}:/reset?token={}", baseUrl, resetPasswordToken);

        sendEmail(email, "Your Space Password Reset", "forgotPasswordEmailTemplate", context);
        logger.info("Reset password email sent to {}", email);
    }
}
