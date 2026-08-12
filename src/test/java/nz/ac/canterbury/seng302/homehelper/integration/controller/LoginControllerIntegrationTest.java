package nz.ac.canterbury.seng302.homehelper.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.LoginController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
public class LoginControllerIntegrationTest {

    @Autowired
    LoginController loginController;

    private MockMvc mockMvc;

    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
    }

    @Test
    public void givenNoParams_WhenGetLoginPage_ThenModelSizeZero() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("loginTemplate"))
                .andExpect(model().size(0)); // Check no error message has been applied
    }

    @Test
    public void givenError_WhenGetLoginPage_ThenErrorExists() throws Exception {
        mockMvc.perform(get("/login")
                        .param("error", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("loginTemplate"))
                .andExpect(model().size(1))
                .andExpect(model().attributeExists("error"));
    }

}
