package com.example.questionboard.entity;

public record ClassActivityStats(
        int questionCount,
        int likeCount,
        int answerCount,
        int solvedCount
) {
}
