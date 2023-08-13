package com.inbank.dengine.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Account {

    private String personalCode;

    private Boolean isDebt;

    private BigDecimal creditModifier;
}
