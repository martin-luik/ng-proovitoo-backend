package ee.ng.events.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ng.events.auth.model.dto.PostAuthRequest;
import ee.ng.events.auth.service.AuthService;
import ee.ng.events.config.CookieConfig;
import ee.ng.events.config.JwtConfig;
import ee.ng.events.config.security.SecurityConfig;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthControllerTest.TestBeans.class})
class AuthControllerTest {

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
    private AuthService authService;

    @Test
    void login() throws Exception {
        String expectedJwt = "anyToken";

        PostAuthRequest postAuthRequest = new PostAuthRequest();
        postAuthRequest.setEmail("any@email.com");
        postAuthRequest.setPassword("anyPassword");

        var query = objectMapper.writeValueAsString(postAuthRequest);

        doReturn(expectedJwt).when(authService).login(postAuthRequest.getEmail(),
                postAuthRequest.getPassword());

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        Matchers.allOf(
                                Matchers.containsString("access_token=" + expectedJwt),
                                Matchers.containsString("HttpOnly"),
                                Matchers.containsString("Path=/"),
                                Matchers.containsString("SameSite=Lax"),
                                Matchers.containsString("Max-Age="))))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL,
                        Matchers.containsString("no-store")))
                .andExpect(content().string(""));

    }

    @Test
    void logout_clearsCookie_andReturns204() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/auth/logout")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        Matchers.allOf(
                                Matchers.containsString("access_token="),
                                Matchers.containsString("Max-Age=0"),
                                Matchers.containsString("HttpOnly"),
                                Matchers.containsString("Path=/"),
                                Matchers.containsString("SameSite=Lax"))))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL,
                        Matchers.containsString("no-store")))
                .andExpect(content().string(""));
    }

    @Test
    void me_returnsUser_whenAuthenticated() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/auth/me")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt()
                        .jwt(jwt -> jwt.claim("sub", "admin@example.com"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void me_returns401_whenAnonymous() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/auth/me")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnauthorized());
    }
}