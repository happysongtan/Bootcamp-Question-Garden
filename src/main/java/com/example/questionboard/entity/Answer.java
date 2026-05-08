package com.example.questionboard.entity;

import java.time.LocalDateTime;

public record Answer(
        Long id,
        Long questionId,
        String content,
        String nickname,
        String password,
        boolean adopted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
