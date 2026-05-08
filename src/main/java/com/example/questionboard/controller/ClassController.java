package com.example.questionboard.controller;

import com.example.questionboard.dto.FlowerGrowthResponse;
import com.example.questionboard.service.QuestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    private final QuestionService questionService;

    public ClassController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/{className}/flower")
    public FlowerGrowthResponse findFlowerGrowth(@PathVariable String className) {
        return questionService.getFlowerGrowth(className);
    }
}
