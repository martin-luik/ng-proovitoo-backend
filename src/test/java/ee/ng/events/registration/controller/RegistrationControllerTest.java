package ee.ng.events.registration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ng.events.config.security.SecurityConfig;
import ee.ng.events.registration.model.dto.PostRegistrationRequest;
import ee.ng.events.registration.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(RegistrationController.class)
@Import(SecurityConfig.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    RegistrationService registrationService;

    private PostRegistrationRequest postRegistrationRequest;

    @BeforeEach
    void setUp() {
        postRegistrationRequest = new PostRegistrationRequest();
        postRegistrationRequest.setFirstName("John");
        postRegistrationRequest.setLastName("Doe");
        postRegistrationRequest.setPersonalCode("20005202719");
    }

    private static Stream<Arguments> blankValues() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of("  ")
        );
    }

    @ParameterizedTest
    @MethodSource("blankValues")
    void registerForEvent_firstNameIsBlank(String firstName) throws Exception {
        postRegistrationRequest.setFirstName(firstName);

        var query = objectMapper.writeValueAsString(postRegistrationRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events/1/registrations")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("firstName")));
    }

    @ParameterizedTest
    @MethodSource("blankValues")
    void registerForEvent_lastNameIsBlank(String lastName) throws Exception {
        postRegistrationRequest.setLastName(lastName);

        var query = objectMapper.writeValueAsString(postRegistrationRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events/1/registrations")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("lastName")));
    }

    private static Stream<Arguments> notValidPersonalCode() {
        return Stream.of(
                null,
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of("11111111111")
        );
    }

    @ParameterizedTest
    @MethodSource("notValidPersonalCode")
    void registerForEvent_personalCodeNotValid(String personalCode) throws Exception {
        postRegistrationRequest.setPersonalCode(personalCode);

        var query = objectMapper.writeValueAsString(postRegistrationRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events/1/registrations")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("personalCode")));
    }
}