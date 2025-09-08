package ee.ng.events.registration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ng.events.common.web.ErrorCode;
import ee.ng.events.config.CookieConfig;
import ee.ng.events.config.JwtConfig;
import ee.ng.events.config.security.SecurityConfig;
import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.registration.model.dto.PostRegistrationRequest;
import ee.ng.events.registration.model.dto.RegistrationDto;
import ee.ng.events.registration.model.entity.RegistrationEntity;
import ee.ng.events.registration.model.mapper.RegistrationMapper;
import ee.ng.events.registration.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(RegistrationController.class)
@Import({SecurityConfig.class, RegistrationControllerTest.TestBeans.class})
class RegistrationControllerTest {

    @TestConfiguration
    static class TestBeans {
        @Bean
        JwtConfig jwtConfig() {
            var jwtConfig = new JwtConfig();
            jwtConfig.setSecret("test-secret");
            jwtConfig.setIssuer("events-app");
            jwtConfig.setExpiryMinutes(60L);
            return jwtConfig;
        }

        @Bean
        CookieConfig cookieConfig() {
            var cookieConfig = new CookieConfig();
            cookieConfig.setName("access_token");
            cookieConfig.setHttpOnly(true);
            cookieConfig.setSecure(false);
            cookieConfig.setSameSite("Lax");
            cookieConfig.setMaxAgeMinutes(60L);
            return cookieConfig;
        }
    }

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
    void postRegistration_firstNameIsBlank(String firstName) throws Exception {
        postRegistrationRequest.setFirstName(firstName);

        var query = objectMapper.writeValueAsString(postRegistrationRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events/1/registrations")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.json()))
                .andExpect(jsonPath("$.message").value(containsString("firstName")));
    }

    @ParameterizedTest
    @MethodSource("blankValues")
    void postRegistration_lastNameIsBlank(String lastName) throws Exception {
        postRegistrationRequest.setLastName(lastName);

        var query = objectMapper.writeValueAsString(postRegistrationRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events/1/registrations")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.json()))
                .andExpect(jsonPath("$.message").value(containsString("lastName")));
    }

    @ParameterizedTest
    @MethodSource("blankValues")
    void postRegistration_personalCodeIsBlank(String personalCode) throws Exception {
        postRegistrationRequest.setPersonalCode(personalCode);

        var query = objectMapper.writeValueAsString(postRegistrationRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events/1/registrations")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.json()))
                .andExpect(jsonPath("$.message").value(containsString("personalCode")));
    }

    @Test
    void postRegistration_success() throws Exception {
        Long eventId = 1L;

        RegistrationDto registrationDto = RegistrationMapper.INSTANCE.toRegistrationDto(eventId, postRegistrationRequest);
        RegistrationEntity registrationEntity = new RegistrationEntity();
        registrationEntity.setPersonalCode(postRegistrationRequest.getPersonalCode());
        registrationEntity.setFirstName(postRegistrationRequest.getFirstName());
        registrationEntity.setLastName(postRegistrationRequest.getLastName());

        EventEntity eventEntity = new EventEntity();
        eventEntity.setId(eventId);
        registrationEntity.setEventEntity(eventEntity);

        var query = objectMapper.writeValueAsString(postRegistrationRequest);

        doReturn(registrationEntity).when(registrationService).save(registrationDto);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events/1/registrations")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value(registrationEntity.getId()))
                .andExpect(jsonPath("$.eventId").value(registrationEntity.getEventEntity().getId()));
    }
}