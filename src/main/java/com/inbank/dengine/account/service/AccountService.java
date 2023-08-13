package com.inbank.dengine.account.service;

import com.inbank.dengine.account.model.Account;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountService {

    Map<String, Account> userAccount = new HashMap<>();

    AccountService(){
        userAccount.put("49002010965",Account.builder().personalCode("49002010965").isDebt(true).creditModifier(BigDecimal.valueOf(100)).build());
        userAccount.put("49002010976",Account.builder().personalCode("49002010976").isDebt(false).creditModifier(BigDecimal.valueOf(100)).build());
        userAccount.put("49002010987",Account.builder().personalCode("49002010987").isDebt(false).creditModifier(BigDecimal.valueOf(300)).build());
        userAccount.put("49002010998",Account.builder().personalCode("49002010998").isDebt(false).creditModifier(BigDecimal.valueOf(1000)).build());
        userAccount.put("49002010999",Account.builder().personalCode("49002010999").isDebt(false).creditModifier(BigDecimal.valueOf(30)).build());

    }

    public Optional<Account> getAccountByPersonalCode (String personalCode) {
        return Optional.ofNullable(userAccount.get(personalCode));
    }
}
