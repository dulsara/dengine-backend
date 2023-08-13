package com.inbank.dengine.decision.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoanDecisionResponseDTO {

    private String decision;
    private BigDecimal loanAmount;
}
