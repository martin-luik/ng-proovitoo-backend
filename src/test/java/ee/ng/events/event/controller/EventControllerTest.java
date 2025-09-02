package ee.ng.events.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ng.events.common.web.ErrorCode;
import ee.ng.events.config.CookieConfig;
import ee.ng.events.config.JwtConfig;
import ee.ng.events.config.security.SecurityConfig;
import ee.ng.events.event.model.dto.EventDto;
import ee.ng.events.event.model.dto.PostEventRequest;
import ee.ng.events.event.model.dto.PostEventResponse;
import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.event.model.mapper.EventMapper;
import ee.ng.events.event.model.projection.EventSummaryProjection;
import ee.ng.events.event.service.EventService;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@Import({SecurityConfig.class, EventControllerTest.TestBeans.class})
class EventControllerTest {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

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
    EventService eventService;

    private PostEventRequest postEventRequest;

    @BeforeEach
    void setUp() {
        postEventRequest = new PostEventRequest();
        postEventRequest.setName("Sample Event");
        postEventRequest.setStartsAt(LocalDateTime.parse("2024-12-31T23:59:59"));
        postEventRequest.setCapacity(100);
    }

    private static Stream<Arguments> blankValues() {
        return Stream.of(
                null,
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of("  ")
        );
    }

    @ParameterizedTest
    @MethodSource("blankValues")
    void postEvent_nameIsBlank(String name) throws Exception {
        postEventRequest.setName(name);

        var query = objectMapper.writeValueAsString(postEventRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.json()))
                .andExpect(jsonPath("$.message").value(containsString("name")));
    }

    @Test
    void postEvent_startsAtIsNull() throws Exception {
        postEventRequest.setStartsAt(null);

        var query = objectMapper.writeValueAsString(postEventRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.json()))
                .andExpect(jsonPath("$.message").value(containsString("startsAt")));
    }

    @Test
    void postEvent_capacityIsNull() throws Exception {
        postEventRequest.setCapacity(null);

        var query = objectMapper.writeValueAsString(postEventRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.json()))
                .andExpect(jsonPath("$.message").value(containsString("capacity")));
    }



    @Test
    void postEvent() throws Exception {
        var query = objectMapper.writeValueAsString(postEventRequest);

        EventDto eventDto = EventMapper.INSTANCE.toEventDto(postEventRequest);
        EventEntity eventEntity = EventMapper.INSTANCE.toEventEntity(eventDto);
        PostEventResponse postEventResponse = EventMapper.INSTANCE.toPostEventResponse(eventEntity);

        doReturn(eventEntity).when(eventService).save(eventDto);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(postEventResponse.getName()))
                .andExpect(jsonPath("$.startsAt").value(postEventResponse.getStartsAt().toString()))
                .andExpect(jsonPath("$.capacity").value(postEventResponse.getCapacity()));
    }

    @Test
    void getAllEvents() throws Exception {
        EventSummaryProjection eventSummaryProjection = mock(EventSummaryProjection.class);
        doReturn(1L).when(eventSummaryProjection).getId();
        doReturn("Sample Event").when(eventSummaryProjection).getName();
        doReturn(LocalDateTime.parse("2024-12-31T23:59:59")).when(eventSummaryProjection).getStartsAt();
        doReturn(1).when(eventSummaryProjection).getCapacity();
        doReturn(1L).when(eventSummaryProjection).getRegistrationsCount();
        doReturn(1).when(eventSummaryProjection).getAvailableSeats();

        var expected = EventMapper.INSTANCE.toGetEventResponses(List.of(eventSummaryProjection));
        var expectedJson = objectMapper.writeValueAsString(expected);

        doReturn(List.of(eventSummaryProjection)).when(eventService).getAllSummaries();

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/v1/events")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }
}