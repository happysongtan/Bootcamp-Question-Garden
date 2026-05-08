package com.example.questionboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerCreateRequest(
        @NotBlank
        @Size(min = 5, max = 2000)
        String content,

        String nickname,

        @NotBlank
        @Size(min = 4)
        String password
) {
}
