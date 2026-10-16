package com.example.banking.controller;

import com.example.banking.dto.*;
import com.example.banking.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final BankingService bankingService;

    public AccountController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    @GetMapping
    public AccountResponse account(Authentication authentication) {
        return bankingService.getAccount(authentication.getName());
    }

    @PostMapping("/deposit")
    public AccountResponse deposit(@Valid @RequestBody AmountRequest body, Authentication authentication) {
        return bankingService.deposit(authentication.getName(), body.amount());
    }

    @PostMapping("/withdraw")
    public AccountResponse withdraw(@Valid @RequestBody AmountRequest body, Authentication authentication) {
        return bankingService.withdraw(authentication.getName(), body.amount());
    }

    @PostMapping("/transfer")
    public AccountResponse transfer(@Valid @RequestBody TransferRequest body, Authentication authentication) {
        return bankingService.transfer(authentication.getName(), body.targetAccountNumber(), body.amount());
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> transactions(Authentication authentication) {
        return bankingService.history(authentication.getName());
    }
}
