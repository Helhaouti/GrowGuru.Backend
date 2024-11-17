package nl.growguru.app.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.services.GrowGuruService;
import nl.growguru.app.services.MailService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.profiles.active=test"
)
@AutoConfigureMockMvc

@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(value = PER_METHOD)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class AuthenticateControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private GrowGuruService growGuruService;

    @MockBean
    private MailService mailService;

    @SneakyThrows
    @Test
    void testSuccessfulRegistration() {
        var registerDto = new GrowGuru.RegisterDto(
                "test",
                "P@ssword123",
                "user@example.com"
        );

        mockMvc.perform(
                        post(AuthenticateController.AUTH_API_BASE + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerDto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();
    }

    @SneakyThrows
    @Test
    void testInvalidLoginCredentials() {
        var loginDto = new GrowGuru.LoginDto("wrongUser", "wrongPassword");

        mockMvc.perform(
                        post(AuthenticateController.AUTH_API_BASE + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginDto))
                )
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void testUserExistsAfterRegistration() {
        var registerDto = new GrowGuru.RegisterDto(
                "newUser",
                "P@ssword123",
                "newuser@example.com"
        );

        mockMvc.perform(
                        post(AuthenticateController.AUTH_API_BASE + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerDto))
                )
                .andExpect(status().isCreated());

        assertNotNull(growGuruService.findByUserName("newUser"));
    }

}