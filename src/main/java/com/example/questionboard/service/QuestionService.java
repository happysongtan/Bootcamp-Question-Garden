package com.example.questionboard.service;

import com.example.questionboard.dto.AnswerResponse;
import com.example.questionboard.dto.FlowerGrowthResponse;
import com.example.questionboard.dto.PasswordRequest;
import com.example.questionboard.dto.QuestionCreateRequest;
import com.example.questionboard.dto.QuestionResponse;
import com.example.questionboard.dto.QuestionSearchCondition;
import com.example.questionboard.dto.QuestionUpdateRequest;
import com.example.questionboard.entity.Answer;
import com.example.questionboard.entity.ClassActivityStats;
import com.example.questionboard.entity.Question;
import com.example.questionboard.entity.QuestionStatus;
import com.example.questionboard.repository.AnswerRepository;
import com.example.questionboard.repository.ClassFlowerRepository;
import com.example.questionboard.repository.QuestionRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

    private static final String DEFAULT_CLASS_NAME = "21반";

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final ClassFlowerRepository classFlowerRepository;

    public QuestionService(QuestionRepository questionRepository, AnswerRepository answerRepository,
            ClassFlowerRepository classFlowerRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.classFlowerRepository = classFlowerRepository;
    }

    @Transactional
    public QuestionResponse createQuestion(QuestionCreateRequest request) {
        Question question = questionRepository.save(
                request,
                defaultClassName(request.className()),
                defaultNickname(request.nickname()),
                joinTags(request.tags())
        );
        return toResponse(question, false);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> findQuestions(QuestionSearchCondition condition) {
        return questionRepository.findAll(condition)
                .stream()
                .map(question -> toResponse(question, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> findPopularQuestions(String className) {
        return questionRepository.findTopLiked(defaultClassName(className), 3)
                .stream()
                .map(question -> toResponse(question, false))
                .toList();
    }

    @Transactional
    public QuestionResponse findQuestion(Long questionId) {
        questionRepository.increaseViewCount(questionId);
        Question question = getQuestion(questionId);
        return toResponse(question, true);
    }

    @Transactional
    public QuestionResponse likeQuestion(Long questionId) {
        ensureQuestionExists(questionId);
        questionRepository.increaseLikeCount(questionId);
        return toResponse(getQuestion(questionId), false);
    }

    @Transactional
    public QuestionResponse unlikeQuestion(Long questionId) {
        ensureQuestionExists(questionId);
        questionRepository.decreaseLikeCount(questionId);
        return toResponse(getQuestion(questionId), false);
    }

    @Transactional
    public QuestionResponse updateQuestion(Long questionId, QuestionUpdateRequest request) {
        Question question = getQuestion(questionId);
        validatePassword(question, request.password());
        questionRepository.update(
                questionId,
                request,
                defaultClassName(request.className()),
                defaultNickname(request.nickname()),
                joinTags(request.tags())
        );
        return toResponse(getQuestion(questionId), true);
    }

    @Transactional
    public void deleteQuestion(Long questionId, PasswordRequest request) {
        Question question = getQuestion(questionId);
        validatePassword(question, request.password());
        questionRepository.deleteById(questionId);
    }

    @Transactional
    public QuestionResponse solveQuestion(Long questionId, PasswordRequest request) {
        Question question = getQuestion(questionId);
        validatePassword(question, request.password());
        questionRepository.updateStatus(questionId, QuestionStatus.SOLVED);
        return toResponse(getQuestion(questionId), true);
    }

    @Transactional
    public void reportQuestion(Long questionId) {
        ensureQuestionExists(questionId);
        questionRepository.report(questionId);
    }

    @Transactional
    public void restoreQuestion(Long questionId) {
        questionRepository.restore(questionId);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getReportedQuestions() {
        return questionRepository.findAllReported()
                .stream()
                .map(q -> toResponse(q, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public FlowerGrowthResponse getFlowerGrowth(String className) {
        String normalizedClassName = defaultClassName(className);
        ClassActivityStats stats = questionRepository.findClassActivityStats(normalizedClassName);
        int growthScore = stats.questionCount() * 3
                + stats.likeCount()
                + stats.answerCount() * 4
                + stats.solvedCount() * 5;
        GrowthStage stage = GrowthStage.fromScore(growthScore);
        String flowerType = classFlowerRepository.findFlowerTypeByClassName(normalizedClassName)
                .orElse("sunflower");
        FlowerProfile flower = FlowerProfile.fromType(flowerType);
        return new FlowerGrowthResponse(
                normalizedClassName,
                flower.type(),
                flower.name(),
                growthScore,
                stage.name(),
                stage.displayName(),
                stage.progressPercent(growthScore),
                stats.questionCount(),
                stats.likeCount(),
                stats.answerCount(),
                stats.solvedCount()
        );
    }

    private Question getQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));
    }

    private void ensureQuestionExists(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new IllegalArgumentException("질문을 찾을 수 없습니다.");
        }
    }

    private QuestionResponse toResponse(Question question, boolean includeAnswers) {
        List<Answer> answers = answerRepository.findByQuestionId(question.id());
        List<AnswerResponse> answerResponses = includeAnswers
                ? answers.stream().map(this::toAnswerResponse).toList()
                : List.of();
        return new QuestionResponse(
                question.id(),
                question.className(),
                question.title(),
                question.content(),
                preview(question.content()),
                question.category(),
                question.nickname(),
                question.status().name(),
                question.likeCount(),
                answers.size(),
                question.viewCount(),
                question.reported(),
                splitTags(question.tags()),
                question.createdAt(),
                answerResponses
        );
    }

    private AnswerResponse toAnswerResponse(Answer answer) {
        return new AnswerResponse(
                answer.id(),
                answer.questionId(),
                answer.content(),
                answer.nickname(),
                answer.createdAt(),
                answer.adopted()
        );
    }

    private String preview(String content) {
        if (content == null || content.length() <= 80) {
            return content;
        }
        return content.substring(0, 80) + "...";
    }

    private String joinTags(List<String> tags) {
        if (tags == null) {
            return "";
        }
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .limit(5)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return List.of(tags.split(","))
                .stream()
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .toList();
    }

    private void validatePassword(Question question, String password) {
        if (!question.password().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    private String defaultNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "익명";
        }
        return nickname;
    }

    private String defaultClassName(String className) {
        if (className == null || className.isBlank()) {
            return DEFAULT_CLASS_NAME;
        }
        return className;
    }

    private record FlowerProfile(String type, String name) {
        private static FlowerProfile fromType(String type) {
            String name = switch (type) {
                case "tulip" -> "튤립";
                case "cherry-blossom" -> "벚꽃";
                case "dandelion" -> "민들레";
                case "lavender" -> "라벤더";
                case "cosmos" -> "코스모스";
                case "rose" -> "장미";
                case "hydrangea" -> "수국";
                case "daisy" -> "데이지";
                case "lily" -> "백합";
                case "peony" -> "작약";
                case "camellia" -> "동백꽃";
                default -> "해바라기";
            };
            return new FlowerProfile(type, name);
        }
    }

    private enum GrowthStage {
        SEED(0, "씨앗"),
        SPROUT(10, "새싹"),
        STEM(25, "줄기"),
        BUD(50, "꽃봉오리"),
        BLOOMING(80, "개화"),
        FULL_BLOOM(120, "만개");

        private final int minimumScore;
        private final String displayName;

        GrowthStage(int minimumScore, String displayName) {
            this.minimumScore = minimumScore;
            this.displayName = displayName;
        }

        private static GrowthStage fromScore(int score) {
            GrowthStage result = SEED;
            for (GrowthStage stage : values()) {
                if (score >= stage.minimumScore) {
                    result = stage;
                }
            }
            return result;
        }

        private String displayName() {
            return displayName;
        }

        private int progressPercent(int score) {
            if (this == FULL_BLOOM) {
                return 100;
            }
            GrowthStage next = values()[ordinal() + 1];
            int span = next.minimumScore - minimumScore;
            return Math.min(99, Math.max(0, (score - minimumScore) * 100 / span));
        }
    }
}
