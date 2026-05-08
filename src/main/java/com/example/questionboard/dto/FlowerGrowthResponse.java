package com.example.questionboard.dto;

public record FlowerGrowthResponse(
        String className,
        String flowerType,
        String flowerName,
        int growthScore,
        String growthStage,
        String growthStageName,
        int growthPercent,
        int questionCount,
        int likeCount,
        int answerCount,
        int solvedCount
) {
}
