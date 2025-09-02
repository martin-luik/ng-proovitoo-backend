package ee.ng.events.auth.controller;

import ee.ng.events.auth.model.dto.GetAuthMeResponse;
import ee.ng.events.auth.model.dto.PostAuthRequest;
import ee.ng.events.auth.service.AuthService;
import ee.ng.events.config.CookieConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieConfig cookieConfig;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody PostAuthRequest request) {
        String jwt = authService.login(request.getEmail(), request.getPassword());

        var cookie = ResponseCookie.from(cookieConfig.getName(), jwt)
                .httpOnly(cookieConfig.isHttpOnly())
                .secure(cookieConfig.isSecure())
                .sameSite(cookieConfig.getSameSite())
                .path("/")
                .maxAge(Duration.ofMinutes(60))
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        var del = ResponseCookie.from(cookieConfig.getName(), "")
                .httpOnly(cookieConfig.isHttpOnly())
                .secure(cookieConfig.isSecure())
                .sameSite(cookieConfig.getSameSite())
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, del.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<GetAuthMeResponse> me(Authentication auth) {
        if (auth == null)
            return ResponseEntity.status(401).build();
        var email = auth.getName();
        var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        return ResponseEntity.ok(
                GetAuthMeResponse.builder()
                        .email(email)
                        .roles(roles)
                        .build());
    }

}
