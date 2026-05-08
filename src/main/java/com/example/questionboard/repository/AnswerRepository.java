package com.example.questionboard.repository;

import com.example.questionboard.dto.AnswerCreateRequest;
import com.example.questionboard.entity.Answer;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class AnswerRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnswerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Answer save(Long questionId, AnswerCreateRequest request, String nickname) {
        LocalDateTime now = LocalDateTime.now();
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO answers
                    (question_id, content, nickname, password, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, questionId);
            ps.setString(2, request.content());
            ps.setString(3, nickname);
            ps.setString(4, request.password());
            ps.setTimestamp(5, Timestamp.valueOf(now));
            ps.setTimestamp(6, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return new Answer(
                key == null ? null : key.longValue(),
                questionId,
                request.content(),
                nickname,
                request.password(),
                false,
                now,
                now
        );
    }

    public List<Answer> findByQuestionId(Long questionId) {
        return jdbcTemplate.query(
                "SELECT * FROM answers WHERE question_id = ? ORDER BY created_at ASC",
                answerRowMapper(),
                questionId
        );
    }

    public int countByQuestionId(Long questionId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM answers WHERE question_id = ?",
                Integer.class,
                questionId
        );
        return count == null ? 0 : count;
    }

    public void resetAdoptedForQuestion(Long questionId) {
        jdbcTemplate.update("UPDATE answers SET adopted = FALSE WHERE question_id = ?", questionId);
    }

    public void adopt(Long answerId) {
        jdbcTemplate.update(
                "UPDATE answers SET adopted = TRUE, updated_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now()),
                answerId
        );
    }

    private RowMapper<Answer> answerRowMapper() {
        return (rs, rowNum) -> new Answer(
                rs.getLong("id"),
                rs.getLong("question_id"),
                rs.getString("content"),
                rs.getString("nickname"),
                rs.getString("password"),
                rs.getBoolean("adopted"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
