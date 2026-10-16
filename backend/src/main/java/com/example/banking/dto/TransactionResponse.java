package com.example.banking.dto;

import com.example.banking.model.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
    Long id,
    TransactionType type,
    BigDecimal amount,
    String direction,
    String otherAccountNumber,
    Instant createdAt
) {}

