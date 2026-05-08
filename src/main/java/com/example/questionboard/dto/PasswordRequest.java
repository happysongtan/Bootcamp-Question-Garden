package com.example.questionboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordRequest(
        @NotBlank
        @Size(min = 4)
        String password
) {
}
