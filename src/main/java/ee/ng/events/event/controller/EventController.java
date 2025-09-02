package ee.ng.events.event.controller;

import ee.ng.events.event.model.dto.GetEventResponse;
import ee.ng.events.event.model.dto.PostEventRequest;
import ee.ng.events.event.model.dto.PostEventResponse;
import ee.ng.events.event.model.entity.EventEntity;
import ee.ng.events.event.model.mapper.EventMapper;
import ee.ng.events.event.model.projection.EventSummaryProjection;
import ee.ng.events.event.service.EventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/v1/events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PostEventResponse> postEvent(@Valid @RequestBody PostEventRequest postEventRequest) {
        EventEntity eventEntity = eventService.save(EventMapper.INSTANCE.toEventDto(postEventRequest));
        return ResponseEntity.ok(EventMapper.INSTANCE.toPostEventResponse(eventEntity));
    }

    @GetMapping
    public ResponseEntity<List<GetEventResponse>> getAllEvents() {
        List<EventSummaryProjection> eventSummaryProjections = eventService.getAllSummaries();
        return ResponseEntity.ok(EventMapper.INSTANCE.toGetEventResponses(eventSummaryProjections));
    }
}
