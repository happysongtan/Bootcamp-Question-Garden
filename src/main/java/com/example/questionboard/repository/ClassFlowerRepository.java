package com.example.questionboard.repository;

import com.example.questionboard.dto.ClassFlowerResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ClassFlowerRepository {

    private final JdbcTemplate jdbcTemplate;

    public ClassFlowerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<String> findFlowerTypeByClassName(String className) {
        List<String> result = jdbcTemplate.queryForList(
                "SELECT flower_type FROM class_flowers WHERE class_name = ?",
                String.class,
                className
        );
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public List<ClassFlowerResponse> findAll() {
        return jdbcTemplate.query(
                "SELECT class_name, flower_type FROM class_flowers ORDER BY class_name",
                (rs, rowNum) -> new ClassFlowerResponse(rs.getString("class_name"), rs.getString("flower_type"))
        );
    }

    public void upsert(String className, String flowerType) {
        int updated = jdbcTemplate.update(
                "UPDATE class_flowers SET flower_type = ? WHERE class_name = ?",
                flowerType, className
        );
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO class_flowers (class_name, flower_type) VALUES (?, ?)",
                    className, flowerType
            );
        }
    }
}
