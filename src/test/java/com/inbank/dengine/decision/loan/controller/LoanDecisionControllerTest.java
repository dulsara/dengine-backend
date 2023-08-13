package com.inbank.dengine.decision.loan.controller;

import com.inbank.dengine.config.user.AppUserDetails;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
class LoanDecisionControllerTest {

    private MockMvc mockMvc;

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

        mockMvc.perform(MockMvcRequestBuilders.get("/api/decisions/loans")
                        .param("personalCode", personalCode)
                        .param("loanAmount", loanAmount.toString())
                        .param("loanPeriod", loanPeriod.toString()))
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

        mockMvc.perform(MockMvcRequestBuilders.get("/api/decisions/loans")
                        .param("personalCode", personalCode)
                        .param("loanAmount", loanAmount.toString())
                        .param("loanPeriod", loanPeriod.toString()))
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
        mockMvc.perform(MockMvcRequestBuilders.get("/api/decisions/loans")
                        .param("personalCode", personalCode)
                        .param("loanAmount", loanAmount.toString())
                        .param("loanPeriod", loanPeriod.toString()))
                .andExpect(status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(INVALID_USER_ACCOUNT + personalCode));
    }
}