package ee.ng.events.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ng.events.auth.model.dto.PostAuthRequest;
import ee.ng.events.auth.service.AuthService;
import ee.ng.events.config.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;


    @Test
    void login()  throws Exception {
        String expectedToken = "anyToken";

        PostAuthRequest postAuthRequest = new PostAuthRequest();
        postAuthRequest.setEmail("any@email.com");
        postAuthRequest.setPassword("anyPassword");

        var query = objectMapper.writeValueAsString(postAuthRequest);

        doReturn(expectedToken).when(authService).login(postAuthRequest.getEmail(), postAuthRequest.getPassword());


        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(query);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(expectedToken));

    }
}