package com.example.questionboard.dto;

public record QuestionSearchCondition(
        String className,
        String keyword,
        String category,
        String sort,
        String status
) {
}
