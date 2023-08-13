package com.inbank.dengine.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inbank.dengine.config.filter.JwtFilter;
import com.inbank.dengine.config.model.AuthenticationRequest;
import com.inbank.dengine.config.user.AppUserDetails;
import com.inbank.dengine.config.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class JwtFilterTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    JwtUtil jWTUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtoken;
    private AppUserDetails userDetails;


    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(jwtFilter).build();
    }

    @Test
    public void correct_jwt_header_with_valid_token() throws Exception {
        userDetails = new AppUserDetails("test-user", "test@123", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        jwtoken = jWTUtil.generateToken(userDetails);
        mockMvc.perform(get("/api/decisions/loans?personalCode=49002010976&loanAmount=3000&loanPeriod=30")
                        .header("Authorization", "Bearer " + jwtoken))
                .andExpect(status().isOk());
    }

    @Test
    public void unsuccessful_flow_jwt_header_with_invalid_token() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJJc3N1ZXIiOiJrZXNoYW5pIiwiVXNlcm5hbWUiOiJBZG1pblVzZXIiLCJleHAiOjE2Nzk3NzYzMjIsImlhdCI6MTY3OTc3NjMyMn0.I6b0UPpvypzd-mjdedpB4vlmlYcc0iz5VZ5l90-6Qrs";
        Assertions.assertThrows(SignatureException.class, () -> mockMvc.perform(get("/api/decisions/loans?personalCode=49002010976&loanAmount=3000&loanPeriod=30")
                .header("Authorization", "Bearer " + invalidToken)));
    }

    @Test
    public void unsuccessful_flow_jwt_header_with_expired_token() {
        String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJpbmJhbmsiLCJleHAiOjE2OTE5NDcxNDIsImlhdCI6MTY5MTkyOTE0Mn0.RTurw3NgarlV7aCPuPFiltdSGYyj997h_v3E4yODuPGBUNRWeQIz6mMVCEvUvfncjaNwkeHfOtSXZztM68plhg";
        Assertions.assertThrows(ExpiredJwtException.class, () -> mockMvc.perform(get("/api/decisions/loans?personalCode=49002010976&loanAmount=3000&loanPeriod=30")
                .header("Authorization", "Bearer " + expiredToken)));
    }

    @Test
    public void successful_flow_in_jwt_authentication_bypass_token() throws Exception {
        AuthenticationRequest authReq = new AuthenticationRequest("test-user", "test@123");
        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authReq)))
                .andExpect(status().isOk());
    }

    @Test
    public void unsuccessful_flow_in_jwt_token_of_invalid_user() {
        userDetails = new AppUserDetails("not-valid-user", "test@123", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        String jwtoken1 = jWTUtil.generateToken(userDetails);
        Assertions.assertThrows(UsernameNotFoundException.class, () -> mockMvc.perform(get("/api/decisions/loans?personalCode=49002010976&loanAmount=3000&loanPeriod=30")
                .header("Authorization", "Bearer " + jwtoken1)));
    }

}
