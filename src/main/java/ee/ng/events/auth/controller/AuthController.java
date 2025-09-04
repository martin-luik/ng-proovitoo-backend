package ee.ng.events.auth.controller;

import ee.ng.events.auth.model.dto.PostAuthRequest;
import ee.ng.events.auth.model.dto.PostAuthResponse;
import ee.ng.events.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<PostAuthResponse> login(@Valid @RequestBody PostAuthRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new PostAuthResponse(token));
    }
}
