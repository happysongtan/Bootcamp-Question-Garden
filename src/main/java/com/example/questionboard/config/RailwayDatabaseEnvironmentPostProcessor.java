package com.example.questionboard.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class RailwayDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "railwayPostgres";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> properties = postgresProperties(environment);
        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        }
    }

    private Map<String, Object> postgresProperties(ConfigurableEnvironment environment) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (StringUtils.hasText(databaseUrl)) {
            return fromDatabaseUrl(databaseUrl);
        }

        String host = environment.getProperty("PGHOST");
        String port = environment.getProperty("PGPORT", "5432");
        String database = environment.getProperty("PGDATABASE");
        String user = environment.getProperty("PGUSER");
        String password = environment.getProperty("PGPASSWORD");

        if (!StringUtils.hasText(host) || !StringUtils.hasText(database)
                || !StringUtils.hasText(user) || !StringUtils.hasText(password)) {
            return Map.of();
        }

        Map<String, Object> properties = commonPostgresProperties();
        properties.put("spring.datasource.url", "jdbc:postgresql://" + host + ":" + port + "/" + database);
        properties.put("spring.datasource.username", user);
        properties.put("spring.datasource.password", password);
        return properties;
    }

    private Map<String, Object> fromDatabaseUrl(String databaseUrl) {
        URI uri = URI.create(databaseUrl.replaceFirst("^postgres://", "postgresql://"));
        String[] userInfo = uri.getUserInfo() == null ? new String[] {"", ""} : uri.getUserInfo().split(":", 2);
        String username = decode(userInfo[0]);
        String password = userInfo.length > 1 ? decode(userInfo[1]) : "";
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();

        Map<String, Object> properties = commonPostgresProperties();
        properties.put("spring.datasource.url", "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath());
        properties.put("spring.datasource.username", username);
        properties.put("spring.datasource.password", password);
        return properties;
    }

    private Map<String, Object> commonPostgresProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
        properties.put("spring.h2.console.enabled", "false");
        properties.put("spring.sql.init.mode", "always");
        properties.put("spring.sql.init.encoding", "UTF-8");
        properties.put("spring.sql.init.data-locations", "classpath:data-postgresql.sql");
        return properties;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
