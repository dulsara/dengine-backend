package com.inbank.dengine.decision.loan.service;

import com.inbank.dengine.account.model.Account;
import com.inbank.dengine.account.service.AccountService;
import com.inbank.dengine.decision.loan.dto.LoanDecisionRequestDTO;
import com.inbank.dengine.decision.loan.dto.LoanDecisionResponseDTO;
import com.inbank.dengine.exception.exceptionType.BadClientException;
import com.inbank.dengine.exception.exceptionType.ServerException;
import com.inbank.dengine.util.GlobalConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanDecisionService {

    private final AccountService accountService;

    public LoanDecisionResponseDTO getLoanDecision(LoanDecisionRequestDTO loanDecisionRequestDTO) {
        Optional<Account> userAccountOptional = accountService.getAccountByPersonalCode(loanDecisionRequestDTO.getPersonalCode());
        validateLoanRequest(loanDecisionRequestDTO);

        if (!userAccountOptional.isPresent()) {
            throw new BadClientException(GlobalConstant.LoanDecisionErrors.INVALID_USER_ACCOUNT + loanDecisionRequestDTO.getPersonalCode());
        }

        if (userAccountOptional.get().getIsDebt()) {
            return LoanDecisionResponseDTO.builder().decision(GlobalConstant.LoanDecisionErrors.USER_IS_HAVING_DEBT + GlobalConstant.LoanDecisionMessages.LOAN_REJECTED)
                    .loanAmount(BigDecimal.ZERO)
                    .build();
        }

        boolean creditModifierShouldValidAndLargerThanZero = userAccountOptional.get().getCreditModifier() != null && userAccountOptional.get().getCreditModifier().compareTo(BigDecimal.ZERO) > 0;

        if (!creditModifierShouldValidAndLargerThanZero) {
            throw new ServerException(GlobalConstant.LoanDecisionErrors.INTERNAL_DATA_ERROR + loanDecisionRequestDTO.getPersonalCode());

        }
        BigDecimal eligibleLoanAmount = userAccountOptional.get().getCreditModifier().multiply(BigDecimal.valueOf(loanDecisionRequestDTO.getLoanPeriod()));
        BigDecimal creditScore = (eligibleLoanAmount.divide(loanDecisionRequestDTO.getLoanAmount(), MathContext.DECIMAL128));

        if (creditScore.compareTo(BigDecimal.ONE) >= 0) {
            //eligible loan amount can't be exceeded the maximum loan amount
            BigDecimal userEligibleLoanAmount = eligibleLoanAmount.compareTo(GlobalConstant.LoanConstraints.MAXIMUM_SUM) >= 0 ? GlobalConstant.LoanConstraints.MAXIMUM_SUM : eligibleLoanAmount;
            return LoanDecisionResponseDTO.builder().decision(GlobalConstant.LoanDecisionMessages.LOAN_APPROVED)
                    .loanAmount(userEligibleLoanAmount)
                    .build();
        }

        // eligible amount can be given as loan amount if it is greater than minimum sum
        if (eligibleLoanAmount.compareTo(GlobalConstant.LoanConstraints.MINIMUM_SUM) >= 0) {
            return LoanDecisionResponseDTO.builder().decision(GlobalConstant.LoanDecisionMessages.LOAN_REJECTED + GlobalConstant.LoanDecisionMessages.SUGGESTED_AMOUNT)
                    .loanAmount(eligibleLoanAmount)
                    .build();
        }
        // eligible amount is less than minimum amount. then user requested value is needed to be adjusted based on months
        int suggestedLoanPeriod = (loanDecisionRequestDTO.getLoanAmount().divide(userAccountOptional.get().getCreditModifier(), MathContext.DECIMAL128)).setScale(0, RoundingMode.UP).intValue();
        // requested amount can be paid within valid period, user is notified with new loan period
        if (suggestedLoanPeriod <= GlobalConstant.LoanConstraints.MAXIMUM_PERIOD) {
            return LoanDecisionResponseDTO.builder().decision(GlobalConstant.LoanDecisionMessages.LOAN_REJECTED + GlobalConstant.LoanDecisionMessages.SUGGESTED_PERIOD + suggestedLoanPeriod)
                    .loanAmount(loanDecisionRequestDTO.getLoanAmount())
                    .build();
        }

        // try to give maximum loan amount , in maximum eligible period
        BigDecimal suggestedMaximumLoanAmount = BigDecimal.valueOf(GlobalConstant.LoanConstraints.MAXIMUM_PERIOD).multiply(userAccountOptional.get().getCreditModifier());
        if (suggestedMaximumLoanAmount.compareTo(GlobalConstant.LoanConstraints.MINIMUM_SUM) >= 0) {
            return LoanDecisionResponseDTO.builder().decision(GlobalConstant.LoanDecisionMessages.LOAN_REJECTED + GlobalConstant.LoanDecisionMessages.SUGGESTED_AMOUNT + " and " + GlobalConstant.LoanDecisionMessages.SUGGESTED_PERIOD + GlobalConstant.LoanConstraints.MAXIMUM_PERIOD)
                    .loanAmount(suggestedMaximumLoanAmount)
                    .build();
        }
        // no suitable loan amount found for requested user
        return LoanDecisionResponseDTO.builder().decision(GlobalConstant.LoanDecisionMessages.LOAN_REJECTED)
                .loanAmount(BigDecimal.ZERO)
                .build();
    }

    private void validateLoanRequest(LoanDecisionRequestDTO loanDecisionRequestDTO) throws BadClientException {
        if (GlobalConstant.LoanConstraints.MINIMUM_SUM.compareTo(loanDecisionRequestDTO.getLoanAmount()) > 0) {
            throw new BadClientException(GlobalConstant.LoanDecisionErrors.LESS_THAN_MINIMUM_AMOUNT);
        } else if (GlobalConstant.LoanConstraints.MAXIMUM_SUM.compareTo(loanDecisionRequestDTO.getLoanAmount()) < 0) {
            throw new BadClientException(GlobalConstant.LoanDecisionErrors.HIGHER_THAN_MAXIMUM_AMOUNT);
        } else if (GlobalConstant.LoanConstraints.MAXIMUM_PERIOD < loanDecisionRequestDTO.getLoanPeriod()) {
            throw new BadClientException(GlobalConstant.LoanDecisionErrors.MORE_THAN_MAXIMUM_PERIOD);
        } else if (GlobalConstant.LoanConstraints.MINIMUM_PERIOD > loanDecisionRequestDTO.getLoanPeriod()) {
            throw new BadClientException(GlobalConstant.LoanDecisionErrors.LESS_THAN_MINIMUM_PERIOD);
        }
    }
}
