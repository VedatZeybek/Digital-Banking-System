package com.example.banking.controller;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.AuthRequest;
import com.example.banking.service.AuthService;
import com.example.banking.service.BankingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final BankingService bankingService;
    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository securityContextRepository =
        new HttpSessionSecurityContextRepository();

    public AuthController(AuthService authService, BankingService bankingService,
                          AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.bankingService = bankingService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public AccountResponse register(@Valid @RequestBody AuthRequest body,
                                    HttpServletRequest request, HttpServletResponse response) {
        authService.register(body);
        return authenticate(body, request, response);
    }

    @PostMapping("/login")
    public AccountResponse login(@Valid @RequestBody AuthRequest body,
                                 HttpServletRequest request, HttpServletResponse response) {
        return authenticate(body, request, response);
    }

    @GetMapping("/me")
    public AccountResponse me(Authentication authentication) {
        return bankingService.getAccount(authentication.getName());
    }

    private AccountResponse authenticate(AuthRequest body, HttpServletRequest request,
                                         HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(body.username().trim(), body.password()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        return bankingService.getAccount(authentication.getName());
    }
}

