package com.example.questionboard.dto;

import java.time.LocalDateTime;
import java.util.List;

public record QuestionResponse(
        Long id,
        String className,
        String title,
        String content,
        String contentPreview,
        String category,
        String nickname,
        String status,
        int likeCount,
        int answerCount,
        int viewCount,
        boolean reported,
        List<String> tags,
        LocalDateTime createdAt,
        List<AnswerResponse> answers
) {
}
