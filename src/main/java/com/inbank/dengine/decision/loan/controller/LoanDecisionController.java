package com.inbank.dengine.decision.loan.controller;

import com.inbank.dengine.decision.loan.dto.LoanDecisionRequestDTO;
import com.inbank.dengine.decision.loan.dto.LoanDecisionResponseDTO;
import com.inbank.dengine.decision.loan.service.LoanDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@CrossOrigin
@RestController
@RequestMapping("api/decisions/loans")
@RequiredArgsConstructor

public class LoanDecisionController {
    private final LoanDecisionService loanDecisionService;

    @GetMapping()
    public ResponseEntity<LoanDecisionResponseDTO> getLoanDecision (@Valid LoanDecisionRequestDTO loanDecisionRequestDTO ) {
        return ResponseEntity.ok().body(loanDecisionService.getLoanDecision(loanDecisionRequestDTO));
    }
}
