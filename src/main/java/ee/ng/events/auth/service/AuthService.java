package ee.ng.events.auth.service;

import ee.ng.events.config.AdminConfig;
import ee.ng.events.config.JwtConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminConfig adminConfig;
    private final JwtConfig jwtConfig;

    public String login(String email, String password) {
        if (!email.equalsIgnoreCase(adminConfig.getEmail()) || !BCrypt.checkpw(password, adminConfig.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(adminConfig.getEmail())
                .issuer(jwtConfig.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtConfig.getExpiryMinutes(), ChronoUnit.MINUTES)))
                .claim("roles", List.of("ADMIN"))
                .signWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
