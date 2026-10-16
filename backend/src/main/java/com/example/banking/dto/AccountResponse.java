package com.example.banking.dto;

import java.math.BigDecimal;

public record AccountResponse(String username, String accountNumber, BigDecimal balance) {}

