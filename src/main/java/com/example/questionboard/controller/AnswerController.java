package com.example.questionboard.controller;

import com.example.questionboard.dto.AnswerCreateRequest;
import com.example.questionboard.dto.AnswerResponse;
import com.example.questionboard.dto.PasswordRequest;
import com.example.questionboard.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions/{questionId}/answers")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnswerResponse createAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerCreateRequest request
    ) {
        return answerService.createAnswer(questionId, request);
    }

    @PatchMapping("/{answerId}/unadopt")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unadoptAnswer(
            @PathVariable Long questionId,
            @PathVariable Long answerId,
            @RequestBody PasswordRequest request
    ) {
        answerService.unadoptAnswer(questionId, answerId, request);
    }

    @PatchMapping("/{answerId}/adopt")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void adoptAnswer(
            @PathVariable Long questionId,
            @PathVariable Long answerId,
            @RequestBody PasswordRequest request
    ) {
        answerService.adoptAnswer(questionId, answerId, request);
    }
}
