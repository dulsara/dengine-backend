package com.inbank.dengine.decision.loan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inbank.dengine.config.user.AppUserDetails;
import com.inbank.dengine.decision.loan.dto.LoanDecisionRequestDTO;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.inbank.dengine.util.GlobalConstant.LoanDecisionErrors.INVALID_USER_ACCOUNT;
import static com.inbank.dengine.util.GlobalConstant.LoanDecisionMessages.LOAN_APPROVED;
import static com.inbank.dengine.util.GlobalConstant.LoanDecisionMessages.LOAN_REJECTED;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class LoanDecisionControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        AppUserDetails userDetails = new AppUserDetails("test-user", "test@123", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.setContext( SecurityContextHolder.createEmptyContext());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

    }

    @SneakyThrows
    @Test
    void successful_approved_loan_request_operation() {

        BigDecimal loanAmount = BigDecimal.valueOf(2000);
        String personalCode = "49002010976";
        Integer loanPeriod = 20;

        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);
        String loanDecisionRequestDTOString = objectMapper.writeValueAsString(loanDecisionRequestDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/decisions/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanDecisionRequestDTOString))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.decision").value(LOAN_APPROVED))
                .andExpect(MockMvcResultMatchers.jsonPath("$.loanAmount").value(loanAmount));
    }

    @SneakyThrows
    @Test
    void successful_rejected_loan_request_operation() {

        BigDecimal loanAmount = BigDecimal.valueOf(7000);
        String personalCode = "49002010999";
        Integer loanPeriod = 15;

        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);
        String loanDecisionRequestDTOString = objectMapper.writeValueAsString(loanDecisionRequestDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/decisions/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanDecisionRequestDTOString))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.decision").value(LOAN_REJECTED))
                .andExpect(MockMvcResultMatchers.jsonPath("$.loanAmount").value(BigDecimal.ZERO));
    }

    @SneakyThrows
    @Test
    void exception_for_user_account_not_found() {

        BigDecimal loanAmount = BigDecimal.valueOf(2000);
        String personalCode = "12345";
        Integer loanPeriod = 20;

        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);
        String loanDecisionRequestDTOString = objectMapper.writeValueAsString(loanDecisionRequestDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/decisions/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanDecisionRequestDTOString))
                .andExpect(status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(INVALID_USER_ACCOUNT + personalCode));
    }

    private LoanDecisionRequestDTO getLoanDecisionRequestDTOObject(String personalCode, BigDecimal loanAmount, Integer loanPeriod) {
        return LoanDecisionRequestDTO.builder()
                .loanAmount(loanAmount)
                .personalCode(personalCode)
                .loanPeriod(loanPeriod)
                .build();
    }
}