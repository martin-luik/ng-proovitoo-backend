package ee.ng.events.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ng.events.common.web.ErrorCode;
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
@Import(SecurityConfig.class)
class EventControllerTest {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

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
        postEventRequest.setTitle("Sample Event");
        postEventRequest.setStartAt(LocalDateTime.parse("2024-12-31T23:59:59"));
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
    void postExResult_titleIsBlank(String title) throws Exception {
        postEventRequest.setTitle(title);

        var query = objectMapper.writeValueAsString(postEventRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.message").value(containsString("title")));
    }

    @Test
    void postExResult_startAtIsNull() throws Exception {
        postEventRequest.setStartAt(null);

        var query = objectMapper.writeValueAsString(postEventRequest);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/v1/events")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query)
                .with(jwt().authorities(new SimpleGrantedAuthority(ROLE_ADMIN)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.message").value(containsString("startAt")));
    }

    @Test
    void postExResult_capacityIsNull() throws Exception {
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
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
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
                .andExpect(jsonPath("$.title").value(postEventResponse.getTitle()))
                .andExpect(jsonPath("$.startAt").value(postEventResponse.getStartAt().toString()))
                .andExpect(jsonPath("$.capacity").value(postEventResponse.getCapacity()));
    }

    @Test
    void getAllEvents() throws Exception {
        EventSummaryProjection eventSummaryProjection = mock(EventSummaryProjection.class);
        doReturn(1L).when(eventSummaryProjection).getId();
        doReturn("Sample Event").when(eventSummaryProjection).getTitle();
        doReturn(LocalDateTime.parse("2024-12-31T23:59:59")).when(eventSummaryProjection).getStartAt();
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