package com.inbank.dengine.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inbank.dengine.config.model.AuthenticationRequest;
import com.inbank.dengine.config.model.AuthenticationResponse;
import com.inbank.dengine.config.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;


    @Autowired
    private JwtUtil jWTUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void successful_authentication_with_valid_credentials() throws Exception {
        AuthenticationRequest authReq = new AuthenticationRequest( "test-user", "test@123");
        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    void successful_authentication_and_return_valid_jwt_token() throws Exception {
        AuthenticationRequest authReq = new AuthenticationRequest( "test-user", "test@123");
        MvcResult result =  mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isOk()).andReturn();

        // Extract the JWT token from the response
        AuthenticationResponse authResponse = new ObjectMapper().readValue(result.getResponse().getContentAsString(), AuthenticationResponse.class);
        assertEquals("test-user", jWTUtil.getUsernameForToken(authResponse.getJwtToken()));
    }

    @Test
    void failed_authentication_with_invalid_username() throws Exception {
        AuthenticationRequest authReq = new AuthenticationRequest( "not-valid-user", "test@123");
        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void failed_authentication_with_invalid_password() throws Exception {
        AuthenticationRequest authReq = new AuthenticationRequest( "test-user", "invalid-password");
        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isUnauthorized());
    }
}