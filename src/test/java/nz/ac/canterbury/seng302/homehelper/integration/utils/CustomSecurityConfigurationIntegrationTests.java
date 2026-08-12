package nz.ac.canterbury.seng302.homehelper.integration.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


@SpringBootTest
@AutoConfigureMockMvc
public class CustomSecurityConfigurationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void logout_loggedIn_cookiesCleared() throws Exception {
        MvcResult result = mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();

        // Check that the JSESSIONID cookie has been cleared
        String setCookieHeader = response.getHeader("Set-Cookie");
        assertTrue(Objects.requireNonNull(setCookieHeader).contains("Max-Age=0")
                || setCookieHeader.contains("Expires=Thu, 01 Jan 1970"));
    }


    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void logout_loggedIn_sessionInvalidated() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // Check that the session is invalidated
        assertTrue(session.isInvalid());
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void logout_loggedIn_userRoleRevoked() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection());

        // Then try to access page requiring user to be logged in
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }


}

