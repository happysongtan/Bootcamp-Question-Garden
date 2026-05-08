package com.example.questionboard.controller;

import com.example.questionboard.dto.PasswordRequest;
import com.example.questionboard.dto.QuestionCreateRequest;
import com.example.questionboard.dto.QuestionResponse;
import com.example.questionboard.dto.QuestionSearchCondition;
import com.example.questionboard.dto.QuestionUpdateRequest;
import com.example.questionboard.service.QuestionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse createQuestion(@Valid @RequestBody QuestionCreateRequest request) {
        return questionService.createQuestion(request);
    }

    @GetMapping
    public List<QuestionResponse> findQuestions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false) String status
    ) {
        return questionService.findQuestions(new QuestionSearchCondition(className, keyword, category, sort, status));
    }

    @GetMapping("/popular")
    public List<QuestionResponse> findPopularQuestions(@RequestParam(required = false) String className) {
        return questionService.findPopularQuestions(className);
    }

    @GetMapping("/{questionId}")
    public QuestionResponse findQuestion(@PathVariable Long questionId) {
        return questionService.findQuestion(questionId);
    }

    @PostMapping("/{questionId}/like")
    public QuestionResponse likeQuestion(@PathVariable Long questionId) {
        return questionService.likeQuestion(questionId);
    }

    @DeleteMapping("/{questionId}/like")
    public QuestionResponse unlikeQuestion(@PathVariable Long questionId) {
        return questionService.unlikeQuestion(questionId);
    }

    @PutMapping("/{questionId}")
    public QuestionResponse updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionUpdateRequest request
    ) {
        return questionService.updateQuestion(questionId, request);
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody PasswordRequest request
    ) {
        questionService.deleteQuestion(questionId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{questionId}/report")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reportQuestion(@PathVariable Long questionId) {
        questionService.reportQuestion(questionId);
    }

    @PatchMapping("/{questionId}/solve")
    public QuestionResponse solveQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody PasswordRequest request
    ) {
        return questionService.solveQuestion(questionId, request);
    }
}
