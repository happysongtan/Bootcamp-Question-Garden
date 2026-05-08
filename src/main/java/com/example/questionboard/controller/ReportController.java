package com.example.questionboard.controller;

import com.example.questionboard.dto.PasswordRequest;
import com.example.questionboard.dto.QuestionResponse;
import com.example.questionboard.service.QuestionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Value("${app.admin.password:admin1234}")
    private String adminPassword;

    private final QuestionService questionService;

    public ReportController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<QuestionResponse> getReportedQuestions(@RequestParam String password) {
        if (!adminPassword.equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸습니다.");
        }
        return questionService.getReportedQuestions();
    }

    @PatchMapping("/{questionId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreQuestion(
            @PathVariable Long questionId,
            @RequestBody PasswordRequest request
    ) {
        if (!adminPassword.equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸습니다.");
        }
        questionService.restoreQuestion(questionId);
    }
}
