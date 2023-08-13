package com.inbank.dengine.decision.loan.service;

import com.inbank.dengine.account.model.Account;
import com.inbank.dengine.account.service.AccountService;
import com.inbank.dengine.decision.loan.dto.LoanDecisionRequestDTO;
import com.inbank.dengine.decision.loan.dto.LoanDecisionResponseDTO;
import com.inbank.dengine.exception.exceptionType.BadClientException;
import com.inbank.dengine.util.GlobalConstant;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static com.inbank.dengine.util.GlobalConstant.LoanDecisionErrors.*;
import static com.inbank.dengine.util.GlobalConstant.LoanDecisionMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class LoanDecisionServiceTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private LoanDecisionService loanDecisionService;


    @SneakyThrows
    @Test
    void successful_loan_approved_request_should_not_throw_any_exception() {
        BigDecimal loanAmount = BigDecimal.valueOf(2000);
        String personalCode = "12345";
        Integer loanPeriod = 20;

        Account account = getAccountObject(personalCode, BigDecimal.valueOf(100), false);
        when(accountService.getAccountByPersonalCode(personalCode)).thenReturn(Optional.of(account));
        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);

        LoanDecisionResponseDTO loanDecisionResponseDTO = loanDecisionService.getLoanDecision(loanDecisionRequestDTO);

        Assertions.assertNotNull(loanDecisionResponseDTO);
        Assertions.assertEquals(loanAmount, loanDecisionResponseDTO.getLoanAmount());
        Assertions.assertEquals(LOAN_APPROVED, loanDecisionResponseDTO.getDecision());
    }

    @SneakyThrows
    @Test
    void successful_loan_approved_request_with_more_than_requested_amount_and_should_not_throw_any_exception() {
        BigDecimal loanAmount = BigDecimal.valueOf(2000);
        String personalCode = "12345";
        Integer loanPeriod = 30;
        BigDecimal creditModifier = BigDecimal.valueOf(100);
        BigDecimal eligibleAmount = creditModifier.multiply(BigDecimal.valueOf(loanPeriod));

        Account account = getAccountObject(personalCode, creditModifier, false);
        when(accountService.getAccountByPersonalCode(personalCode)).thenReturn(Optional.of(account));
        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);

        LoanDecisionResponseDTO loanDecisionResponseDTO = loanDecisionService.getLoanDecision(loanDecisionRequestDTO);

        Assertions.assertNotNull(loanDecisionResponseDTO);
        Assertions.assertEquals(eligibleAmount, loanDecisionResponseDTO.getLoanAmount());
        Assertions.assertEquals(LOAN_APPROVED, loanDecisionResponseDTO.getDecision());
    }

    @SneakyThrows
    @Test
    void rejected_loan_request_with_bank_suggested_loan_amount_and_should_not_throw_any_exception() {
        BigDecimal loanAmount = BigDecimal.valueOf(4000);
        String personalCode = "12345";
        Integer loanPeriod = 30;
        BigDecimal creditModifier = BigDecimal.valueOf(100);
        BigDecimal eligibleAmount = creditModifier.multiply(BigDecimal.valueOf(loanPeriod));

        Account account = getAccountObject(personalCode, creditModifier, false);
        when(accountService.getAccountByPersonalCode(personalCode)).thenReturn(Optional.of(account));
        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);

        LoanDecisionResponseDTO loanDecisionResponseDTO = loanDecisionService.getLoanDecision(loanDecisionRequestDTO);

        Assertions.assertNotNull(loanDecisionResponseDTO);
        Assertions.assertEquals(eligibleAmount, loanDecisionResponseDTO.getLoanAmount());
        Assertions.assertEquals(LOAN_REJECTED + SUGGESTED_AMOUNT, loanDecisionResponseDTO.getDecision());
    }

    @SneakyThrows
    @Test
    void rejected_loan_request_with_bank_suggested_loan_period_and_should_not_throw_any_exception() {
        BigDecimal loanAmount = BigDecimal.valueOf(3000);
        String personalCode = "12345";
        Integer loanPeriod = 15;
        Integer suggestedLoanPeriod = 30;
        BigDecimal creditModifier = BigDecimal.valueOf(100);

        Account account = getAccountObject(personalCode, creditModifier, false);
        when(accountService.getAccountByPersonalCode(personalCode)).thenReturn(Optional.of(account));
        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);

        LoanDecisionResponseDTO loanDecisionResponseDTO = loanDecisionService.getLoanDecision(loanDecisionRequestDTO);

        Assertions.assertNotNull(loanDecisionResponseDTO);
        Assertions.assertEquals(loanAmount, loanDecisionResponseDTO.getLoanAmount());
        Assertions.assertEquals(LOAN_REJECTED + SUGGESTED_PERIOD + suggestedLoanPeriod, loanDecisionResponseDTO.getDecision());
    }

    @SneakyThrows
    @Test
    void rejected_loan_request_with_both_bank_suggested_loan_amount_and_period_and_should_not_throw_any_exception() {
        BigDecimal loanAmount = BigDecimal.valueOf(7000);
        String personalCode = "12345";
        Integer loanPeriod = 15;
        BigDecimal creditModifier = BigDecimal.valueOf(100);

        Account account = getAccountObject(personalCode, creditModifier, false);
        when(accountService.getAccountByPersonalCode(personalCode)).thenReturn(Optional.of(account));
        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);

        LoanDecisionResponseDTO loanDecisionResponseDTO = loanDecisionService.getLoanDecision(loanDecisionRequestDTO);

        Assertions.assertNotNull(loanDecisionResponseDTO);
        Assertions.assertEquals(BigDecimal.valueOf(6000),loanDecisionResponseDTO.getLoanAmount());
        Assertions.assertEquals(GlobalConstant.LoanDecisionMessages.LOAN_REJECTED + GlobalConstant.LoanDecisionMessages.SUGGESTED_AMOUNT + " and " + GlobalConstant.LoanDecisionMessages.SUGGESTED_PERIOD + GlobalConstant.LoanConstraints.MAXIMUM_PERIOD, loanDecisionResponseDTO.getDecision());
    }

    @SneakyThrows
    @Test
    void rejected_loan_request_and_should_not_throw_any_exception() {
        BigDecimal loanAmount = BigDecimal.valueOf(7000);
        String personalCode = "12345";
        Integer loanPeriod = 15;
        BigDecimal creditModifier = BigDecimal.valueOf(20);
        int suggestedLoanPeriodForMinimumAmount = (GlobalConstant.LoanConstraints.MINIMUM_SUM.divide(creditModifier)).setScale(0, RoundingMode.UP).intValue();

        Account account = getAccountObject(personalCode, creditModifier, false);
        when(accountService.getAccountByPersonalCode(personalCode)).thenReturn(Optional.of(account));
        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);

        LoanDecisionResponseDTO loanDecisionResponseDTO = loanDecisionService.getLoanDecision(loanDecisionRequestDTO);

        Assertions.assertNotNull(loanDecisionResponseDTO);
        Assertions.assertEquals(BigDecimal.ZERO, loanDecisionResponseDTO.getLoanAmount());
        Assertions.assertEquals(LOAN_REJECTED, loanDecisionResponseDTO.getDecision());
    }

    @SneakyThrows
    @Test
    void rejected_loan_request_due_to_user_is_having_debt_and_should_not_throw_any_exception() {
        BigDecimal loanAmount = BigDecimal.valueOf(7000);
        String personalCode = "12345";
        Integer loanPeriod = 15;
        BigDecimal creditModifier = BigDecimal.valueOf(100);

        Account account = getAccountObject(personalCode, creditModifier, true);
        when(accountService.getAccountByPersonalCode(personalCode)).thenReturn(Optional.of(account));
        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);

        LoanDecisionResponseDTO loanDecisionResponseDTO = loanDecisionService.getLoanDecision(loanDecisionRequestDTO);

        Assertions.assertNotNull(loanDecisionResponseDTO);
        Assertions.assertEquals(BigDecimal.ZERO, loanDecisionResponseDTO.getLoanAmount());
        Assertions.assertEquals(USER_IS_HAVING_DEBT + LOAN_REJECTED, loanDecisionResponseDTO.getDecision());
    }

    @Test
    void loan_request_should_throw_user_account_not_found_exception() {
        BigDecimal loanAmount = BigDecimal.valueOf(3000);
        String personalCode = "12345";
        Integer loanPeriod = 15;
        BigDecimal creditModifier = BigDecimal.valueOf(100);

        when(accountService.getAccountByPersonalCode(personalCode)).thenReturn(Optional.ofNullable(null));
        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);

        Exception exception = assertThrows(BadClientException.class, () -> {
            LoanDecisionResponseDTO loanDecisionResponseDTO = loanDecisionService.getLoanDecision(loanDecisionRequestDTO);
        });

        String expectedMessage = INVALID_USER_ACCOUNT + personalCode;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.equals(expectedMessage));

    }

    @Test
    void loan_request_should_throw_higher_requested_amount_exception() {
        BigDecimal loanAmount = BigDecimal.valueOf(10001);
        String personalCode = "12345";
        Integer loanPeriod = 15;

        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);

        Exception exception = assertThrows(BadClientException.class, () -> {
            LoanDecisionResponseDTO loanDecisionResponseDTO = loanDecisionService.getLoanDecision(loanDecisionRequestDTO);
        });

        String expectedMessage = HIGHER_THAN_MAXIMUM_AMOUNT;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.equals(expectedMessage));

    }

    @Test
    void loan_request_should_throw_less_than_requested_amount_exception() {
        BigDecimal loanAmount = BigDecimal.valueOf(1999);
        String personalCode = "12345";
        Integer loanPeriod = 15;

        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);

        Exception exception = assertThrows(BadClientException.class, () -> {
            LoanDecisionResponseDTO loanDecisionResponseDTO = loanDecisionService.getLoanDecision(loanDecisionRequestDTO);
        });

        String expectedMessage = LESS_THAN_MINIMUM_AMOUNT;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.equals(expectedMessage));

    }

    @Test
    void loan_request_should_throw_less_than_requested_loan_period_exception() {
        BigDecimal loanAmount = BigDecimal.valueOf(3000);
        String personalCode = "12345";
        Integer loanPeriod = 11;

        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);

        Exception exception = assertThrows(BadClientException.class, () -> {
            LoanDecisionResponseDTO loanDecisionResponseDTO = loanDecisionService.getLoanDecision(loanDecisionRequestDTO);
        });

        String expectedMessage = LESS_THAN_MINIMUM_PERIOD;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.equals(expectedMessage));

    }

    @Test
    void loan_request_should_throw_higher_than_requested_loan_period_exception() {
        BigDecimal loanAmount = BigDecimal.valueOf(3000);
        String personalCode = "12345";
        Integer loanPeriod = 61;

        LoanDecisionRequestDTO loanDecisionRequestDTO = getLoanDecisionRequestDTOObject(personalCode, loanAmount, loanPeriod);

        Exception exception = assertThrows(BadClientException.class, () -> {
            LoanDecisionResponseDTO loanDecisionResponseDTO = loanDecisionService.getLoanDecision(loanDecisionRequestDTO);
        });

        String expectedMessage = MORE_THAN_MAXIMUM_PERIOD;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.equals(expectedMessage));

    }

    private LoanDecisionRequestDTO getLoanDecisionRequestDTOObject(String personalCode, BigDecimal loanAmount, Integer loanPeriod) {
        return LoanDecisionRequestDTO.builder()
                .loanAmount(loanAmount)
                .personalCode(personalCode)
                .loanPeriod(loanPeriod)
                .build();
    }

    private Account getAccountObject(String personalCode, BigDecimal creditModifier, Boolean isDebt) {
        return Account.builder()
                .creditModifier(creditModifier)
                .personalCode(personalCode)
                .isDebt(isDebt)
                .build();
    }
}