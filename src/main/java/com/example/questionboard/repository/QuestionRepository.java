package com.example.questionboard.repository;

import com.example.questionboard.dto.QuestionCreateRequest;
import com.example.questionboard.dto.QuestionSearchCondition;
import com.example.questionboard.dto.QuestionUpdateRequest;
import com.example.questionboard.entity.ClassActivityStats;
import com.example.questionboard.entity.Question;
import com.example.questionboard.entity.QuestionStatus;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class QuestionRepository {

    private final JdbcTemplate jdbcTemplate;

    public QuestionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Question save(QuestionCreateRequest request, String className, String nickname, String tags) {
        LocalDateTime now = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO questions
                    (class_name, title, content, category, nickname, password, status, like_count, view_count, tags, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, 0, 0, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, className);
            ps.setString(2, request.title());
            ps.setString(3, request.content());
            ps.setString(4, request.category());
            ps.setString(5, nickname);
            ps.setString(6, request.password());
            ps.setString(7, QuestionStatus.WAITING.name());
            ps.setString(8, tags);
            ps.setTimestamp(9, Timestamp.valueOf(now));
            ps.setTimestamp(10, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        Long id = key == null ? null : key.longValue();
        return findById(id).orElseThrow();
    }

    public List<Question> findAll(QuestionSearchCondition condition) {
        StringBuilder sql = new StringBuilder("SELECT * FROM questions WHERE reported = FALSE");
        List<Object> params = new ArrayList<>();

        if (StringUtils.hasText(condition.keyword())) {
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(content) LIKE ? OR LOWER(tags) LIKE ? OR LOWER(category) LIKE ?)");
            String keyword = "%" + condition.keyword().toLowerCase() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        if (StringUtils.hasText(condition.className())) {
            sql.append(" AND class_name = ?");
            params.add(condition.className());
        }

        if (StringUtils.hasText(condition.category()) && !"전체".equals(condition.category())) {
            sql.append(" AND category = ?");
            params.add(condition.category());
        }

        if (StringUtils.hasText(condition.status())) {
            sql.append(" AND status = ?");
            params.add(condition.status());
        }

        sql.append(orderBy(condition.sort()));
        return jdbcTemplate.query(sql.toString(), questionRowMapper(), params.toArray());
    }

    public List<Question> findTopLiked(String className, int limit) {
        String weekStart = "DATE_TRUNC('week', CURRENT_TIMESTAMP)";
        if (StringUtils.hasText(className)) {
            return jdbcTemplate.query(
                    "SELECT * FROM questions WHERE class_name = ? AND reported = FALSE AND created_at >= " + weekStart + " ORDER BY like_count DESC, created_at DESC LIMIT ?",
                    questionRowMapper(),
                    className,
                    limit
            );
        }
        return jdbcTemplate.query(
                "SELECT * FROM questions WHERE reported = FALSE AND created_at >= " + weekStart + " ORDER BY like_count DESC, created_at DESC LIMIT ?",
                questionRowMapper(),
                limit
        );
    }

    public Optional<Question> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        List<Question> questions = jdbcTemplate.query(
                "SELECT * FROM questions WHERE id = ?",
                questionRowMapper(),
                id
        );
        return questions.stream().findFirst();
    }

    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM questions WHERE id = ?",
                Integer.class,
                id
        );
        return count != null && count > 0;
    }

    public void increaseLikeCount(Long id) {
        jdbcTemplate.update("UPDATE questions SET like_count = like_count + 1, updated_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now()),
                id
        );
    }

    public void decreaseLikeCount(Long id) {
        jdbcTemplate.update("""
                UPDATE questions
                SET like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END,
                    updated_at = ?
                WHERE id = ?
                """,
                Timestamp.valueOf(LocalDateTime.now()),
                id
        );
    }

    public void increaseViewCount(Long id) {
        jdbcTemplate.update("UPDATE questions SET view_count = view_count + 1 WHERE id = ?", id);
    }

    public void updateStatus(Long id, QuestionStatus status) {
        jdbcTemplate.update("UPDATE questions SET status = ?, updated_at = ? WHERE id = ?",
                status.name(),
                Timestamp.valueOf(LocalDateTime.now()),
                id
        );
    }

    public void update(Long id, QuestionUpdateRequest request, String className, String nickname, String tags) {
        jdbcTemplate.update("""
                UPDATE questions
                SET class_name = ?, title = ?, content = ?, category = ?, nickname = ?, tags = ?, updated_at = ?
                WHERE id = ?
                """,
                className,
                request.title(),
                request.content(),
                request.category(),
                nickname,
                tags,
                Timestamp.valueOf(LocalDateTime.now()),
                id
        );
    }

    public ClassActivityStats findClassActivityStats(String className) {
        return jdbcTemplate.queryForObject("""
                SELECT
                    (SELECT COUNT(*) FROM questions WHERE class_name = ?) AS question_count,
                    (SELECT COALESCE(SUM(like_count), 0) FROM questions WHERE class_name = ?) AS like_count,
                    (
                        SELECT COUNT(*)
                        FROM answers
                        JOIN questions ON answers.question_id = questions.id
                        WHERE questions.class_name = ?
                    ) AS answer_count,
                    (SELECT COUNT(*) FROM questions WHERE class_name = ? AND status = 'SOLVED') AS solved_count
                """,
                (rs, rowNum) -> new ClassActivityStats(
                        rs.getInt("question_count"),
                        rs.getInt("like_count"),
                        rs.getInt("answer_count"),
                        rs.getInt("solved_count")
                ),
                className,
                className,
                className,
                className
        );
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM questions WHERE id = ?", id);
    }

    private String orderBy(String sort) {
        if ("likes".equals(sort)) {
            return " ORDER BY like_count DESC, created_at DESC";
        }
        if ("answers".equals(sort)) {
            return """
                    ORDER BY (
                        SELECT COUNT(*)
                        FROM answers
                        WHERE answers.question_id = questions.id
                    ) DESC, created_at DESC
                    """;
        }
        if ("waiting".equals(sort)) {
            return " ORDER BY CASE WHEN status = 'WAITING' THEN 0 ELSE 1 END, created_at DESC";
        }
        if ("solved".equals(sort)) {
            return " ORDER BY CASE WHEN status = 'SOLVED' THEN 0 ELSE 1 END, created_at DESC";
        }
        return " ORDER BY created_at DESC";
    }

    public void report(Long id) {
        jdbcTemplate.update("UPDATE questions SET reported = TRUE WHERE id = ?", id);
    }

    public void restore(Long id) {
        jdbcTemplate.update("UPDATE questions SET reported = FALSE WHERE id = ?", id);
    }

    public List<Question> findAllReported() {
        return jdbcTemplate.query(
                "SELECT * FROM questions WHERE reported = TRUE ORDER BY created_at DESC",
                questionRowMapper()
        );
    }

    private RowMapper<Question> questionRowMapper() {
        return (rs, rowNum) -> new Question(
                rs.getLong("id"),
                rs.getString("class_name"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("category"),
                rs.getString("nickname"),
                rs.getString("password"),
                QuestionStatus.valueOf(rs.getString("status")),
                rs.getInt("like_count"),
                rs.getInt("view_count"),
                rs.getBoolean("reported"),
                rs.getString("tags"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
