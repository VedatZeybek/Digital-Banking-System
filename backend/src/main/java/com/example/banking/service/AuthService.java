package com.example.banking.service;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.AuthRequest;
import com.example.banking.exception.BusinessException;
import com.example.banking.model.Account;
import com.example.banking.model.AppUser;
import com.example.banking.model.BankTransaction;
import com.example.banking.model.TransactionType;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
public class AuthService {
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");
    private final UserRepository users;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, AccountRepository accounts,
                       TransactionRepository transactions, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.accounts = accounts;
        this.transactions = transactions;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AccountResponse register(AuthRequest request) {
        String username = request.username().trim();
        if (users.existsByUsername(username)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Username is already in use");
        }
        AppUser user = users.save(new AppUser(username, passwordEncoder.encode(request.password())));
        Account account = accounts.save(new Account(generateAccountNumber(), INITIAL_BALANCE, user));
        transactions.save(new BankTransaction(TransactionType.INITIAL_BALANCE, INITIAL_BALANCE, null, account));
        return new AccountResponse(username, account.getAccountNumber(), account.getBalance());
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "TR" + String.format("%016d", random.nextLong(100_000_000_000_000L, 1_000_000_000_000_000L));
        } while (accounts.existsByAccountNumber(number));
        return number;
    }
}

