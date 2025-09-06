package ee.ng.events.registration.controller;

import ee.ng.events.registration.model.dto.PostRegistrationRequest;
import ee.ng.events.registration.model.dto.PostRegistrationResponse;
import ee.ng.events.registration.model.dto.RegistrationDto;
import ee.ng.events.registration.model.entity.RegistrationEntity;
import ee.ng.events.registration.model.mapper.RegistrationMapper;
import ee.ng.events.registration.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/events/{eventId}/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<PostRegistrationResponse> postRegistration(
            @PathVariable("eventId") Long eventId,
            @Valid @RequestBody PostRegistrationRequest postRegistrationRequest
    ) {
        RegistrationDto registrationDto = RegistrationMapper.INSTANCE.toRegistrationDto(eventId, postRegistrationRequest);
        RegistrationEntity registrationEntity = registrationService.save(registrationDto);
        return ResponseEntity.ok(RegistrationMapper.INSTANCE.toPostRegistrationResponse(registrationEntity));
    }
}
