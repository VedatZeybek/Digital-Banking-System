package com.example.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRequest(
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Username may contain letters, numbers and underscores only")
    @Size(min = 3, max = 50) String username,
    @NotBlank @Size(min = 6, max = 100) String password
) {}

