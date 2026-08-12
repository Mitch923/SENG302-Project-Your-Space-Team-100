package nz.ac.canterbury.seng302.homehelper.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * Custom Authentication Handler to control what happens when authentication fails.
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserService userService;
    Logger logger = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    @Autowired
    public CustomAuthenticationFailureHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles what happens when authentication fails. If the exception is a DisabledException, this
     * means the user's account was not verified yet, and they should be redirected to verify their
     * account. Other authentication exceptions aren't currently handled here.
     *
     * @param request   HttpServletRequest
     * @param response  HttpServletResponse
     * @param exception Authentication exception thrown during the authentication process
     * @throws IOException IOException
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        logger.info(String.valueOf(exception));
        if (exception instanceof DisabledException) {
            request.getParameterMap().forEach((key, value) -> {
                logger.info("Parameter: {} = {}", key, String.join(", ", value));
            });
            String userEmail = request.getParameter("email");
            Long userId = userService.getUserByEmail(userEmail).get().getId();
            logger.info("Unverified user. Redirecting to /verification");
            response.sendRedirect(request.getContextPath() + "/verification?userId=" + userId);
        } else {
            String responseString = request.getContextPath() + "/login?error=true";
            if (request.getParameter("email") != null && !Objects.equals(
                    request.getParameter("email"), "")) {
                responseString = responseString + "&email=" + Base64.getEncoder()
                        .encodeToString(request.getParameter("email").getBytes());
            }
            response.sendRedirect(responseString);
        }

    }

}