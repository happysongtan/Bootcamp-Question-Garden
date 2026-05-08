package com.example.questionboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record QuestionCreateRequest(
        String className,

        @NotBlank
        @Size(min = 2, max = 100)
        String title,

        @NotBlank
        @Size(min = 5, max = 2000)
        String content,

        @NotBlank
        String category,

        String nickname,

        @NotBlank
        @Size(min = 4)
        String password,

        @Size(max = 5)
        List<String> tags
) {
}
