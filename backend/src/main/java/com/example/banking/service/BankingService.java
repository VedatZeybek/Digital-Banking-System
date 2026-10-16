package com.example.banking.service;

import com.example.banking.dto.AccountResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.exception.BusinessException;
import com.example.banking.model.Account;
import com.example.banking.model.BankTransaction;
import com.example.banking.model.TransactionType;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class BankingService {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public BankingService(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String username) {
        return toAccountResponse(findByUsername(username));
    }

    @Transactional
    public AccountResponse deposit(String username, BigDecimal requestedAmount) {
        BigDecimal amount = normalize(requestedAmount);
        Account current = findByUsername(username);
        Account account = lock(current.getId());
        account.setBalance(account.getBalance().add(amount));
        transactions.save(new BankTransaction(TransactionType.DEPOSIT, amount, null, account));
        return toAccountResponse(account);
    }

    @Transactional
    public AccountResponse withdraw(String username, BigDecimal requestedAmount) {
        BigDecimal amount = normalize(requestedAmount);
        Account current = findByUsername(username);
        Account account = lock(current.getId());
        ensureFunds(account, amount);
        account.setBalance(account.getBalance().subtract(amount));
        transactions.save(new BankTransaction(TransactionType.WITHDRAWAL, amount, account, null));
        return toAccountResponse(account);
    }

    @Transactional
    public AccountResponse transfer(String username, String targetAccountNumber, BigDecimal requestedAmount) {
        BigDecimal amount = normalize(requestedAmount);
        Account sourceSnapshot = findByUsername(username);
        Account targetSnapshot = accounts.findByAccountNumber(targetAccountNumber.trim())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Target account was not found"));
        if (sourceSnapshot.getId().equals(targetSnapshot.getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You cannot transfer to your own account");
        }

        Long firstId = Math.min(sourceSnapshot.getId(), targetSnapshot.getId());
        Long secondId = Math.max(sourceSnapshot.getId(), targetSnapshot.getId());
        Account first = lock(firstId);
        Account second = lock(secondId);
        Account source = sourceSnapshot.getId().equals(firstId) ? first : second;
        Account target = targetSnapshot.getId().equals(firstId) ? first : second;

        ensureFunds(source, amount);
        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));
        transactions.save(new BankTransaction(TransactionType.TRANSFER, amount, source, target));
        return toAccountResponse(source);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> history(String username) {
        Account account = findByUsername(username);
        return transactions.findHistory(account.getId()).stream()
            .map(transaction -> toTransactionResponse(transaction, account.getId()))
            .toList();
    }

    private Account findByUsername(String username) {
        return accounts.findByUserUsername(username)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Account was not found"));
    }

    private Account lock(Long id) {
        return accounts.findByIdForUpdate(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Account was not found"));
    }

    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Amount may have at most two decimal places");
        }
    }

    private void ensureFunds(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(account.getUser().getUsername(), account.getAccountNumber(), account.getBalance());
    }

    private TransactionResponse toTransactionResponse(BankTransaction transaction, Long accountId) {
        boolean outgoing = transaction.getFromAccount() != null
            && transaction.getFromAccount().getId().equals(accountId);
        String direction = outgoing ? "OUT" : "IN";
        Account other = outgoing ? transaction.getToAccount() : transaction.getFromAccount();
        return new TransactionResponse(
            transaction.getId(), transaction.getType(), transaction.getAmount(), direction,
            other == null ? null : other.getAccountNumber(), transaction.getCreatedAt());
    }
}

