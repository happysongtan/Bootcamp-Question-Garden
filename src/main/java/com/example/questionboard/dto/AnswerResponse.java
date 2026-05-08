package com.example.questionboard.dto;

import java.time.LocalDateTime;

public record AnswerResponse(
        Long id,
        Long questionId,
        String content,
        String nickname,
        LocalDateTime createdAt,
        boolean adopted
) {
}
