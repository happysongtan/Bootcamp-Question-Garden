package com.example.questionboard.service;

import com.example.questionboard.dto.AnswerCreateRequest;
import com.example.questionboard.dto.AnswerResponse;
import com.example.questionboard.dto.PasswordRequest;
import com.example.questionboard.entity.Answer;
import com.example.questionboard.entity.Question;
import com.example.questionboard.entity.QuestionStatus;
import com.example.questionboard.repository.AnswerRepository;
import com.example.questionboard.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    public AnswerService(AnswerRepository answerRepository, QuestionRepository questionRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public AnswerResponse createAnswer(Long questionId, AnswerCreateRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));
        Answer answer = answerRepository.save(questionId, request, defaultNickname(request.nickname()));

        if (question.status() == QuestionStatus.WAITING) {
            questionRepository.updateStatus(questionId, QuestionStatus.ANSWERED);
        }

        return new AnswerResponse(
                answer.id(),
                answer.questionId(),
                answer.content(),
                answer.nickname(),
                answer.createdAt(),
                answer.adopted()
        );
    }

    @Transactional
    public void unadoptAnswer(Long questionId, Long answerId, PasswordRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));
        if (!question.password().equals(request.password())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        answerRepository.resetAdoptedForQuestion(questionId);
        int answerCount = answerRepository.countByQuestionId(questionId);
        QuestionStatus newStatus = answerCount > 0 ? QuestionStatus.ANSWERED : QuestionStatus.WAITING;
        questionRepository.updateStatus(questionId, newStatus);
    }

    @Transactional
    public void adoptAnswer(Long questionId, Long answerId, PasswordRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));
        if (!question.password().equals(request.password())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        answerRepository.resetAdoptedForQuestion(questionId);
        answerRepository.adopt(answerId);
        questionRepository.updateStatus(questionId, QuestionStatus.SOLVED);
    }

    private String defaultNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "익명";
        }
        return nickname;
    }
}
