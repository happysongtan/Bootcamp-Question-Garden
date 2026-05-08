package com.example.questionboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.questionboard.config.RailwayDatabaseEnvironmentPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class RailwayDatabaseEnvironmentPostProcessorTests {

    @Test
    void convertsRailwayDatabaseUrlToJdbcProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgres://user:secret@db.railway.internal:5432/railway");

        new RailwayDatabaseEnvironmentPostProcessor().postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db.railway.internal:5432/railway");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("user");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("secret");
        assertThat(environment.getProperty("spring.datasource.driver-class-name"))
                .isEqualTo("org.postgresql.Driver");
        assertThat(environment.getProperty("spring.sql.init.data-locations"))
                .isEqualTo("classpath:data-postgresql.sql");
    }

    @Test
    void convertsRailwayPgHostVariablesToJdbcProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("PGHOST", "postgres.railway.internal")
                .withProperty("PGPORT", "5432")
                .withProperty("PGDATABASE", "railway")
                .withProperty("PGUSER", "postgres")
                .withProperty("PGPASSWORD", "secret");

        new RailwayDatabaseEnvironmentPostProcessor().postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://postgres.railway.internal:5432/railway");
        assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("postgres");
        assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("secret");
    }
}
