package com.inbank.dengine.decision.loan.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoanDecisionRequestDTO {
    @NotNull(message = "Personal Code is mandatory for Loan Decision Operation")
    private String personalCode;

    @NotNull(message = "Loan Amount  is mandatory for Loan Decision Operation")
    @Positive(message = "Loan Amount  should be greater than 0")
    private BigDecimal loanAmount;

    @NotNull(message = "Loan Period  is mandatory for Loan Decision Operation")
    @Positive(message = "Loan Period  should be greater than 0")
    private Integer loanPeriod;
}
