package com.example.questionboard.entity;

import java.time.LocalDateTime;

public record Question(
        Long id,
        String className,
        String title,
        String content,
        String category,
        String nickname,
        String password,
        QuestionStatus status,
        int likeCount,
        int viewCount,
        boolean reported,
        String tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
