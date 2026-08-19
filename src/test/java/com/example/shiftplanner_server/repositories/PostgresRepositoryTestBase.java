package com.example.shiftplanner_server.repositories;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
public abstract class PostgresRepositoryTestBase {


    protected void createTablesFromSql(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS sp");
        jdbcTemplate.execute("SET search_path TO sp");

        String script;
        try {
            ClassPathResource resource = new ClassPathResource("db/migration/V1__init_tables.sql");
            if (!resource.exists()) {
                resource = new ClassPathResource("database/table.sql");
            }

            try (InputStream inputStream = resource.getInputStream()) {
                script = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read the SQL schema script for test setup", ex);
        }

        StringBuilder cleanedScript = new StringBuilder();
        for (String line : script.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--")) {
                continue;
            }
            cleanedScript.append(line).append('\n');
        }

        for (String statement : cleanedScript.toString().split(";")) {
            String sql = statement.trim();
            if (sql.isEmpty()) {
                continue;
            }

            String keyword = sql.toUpperCase(Locale.ROOT);
            if ("BEGIN".equals(keyword) || "COMMIT".equals(keyword) || "END".equals(keyword)) {
                continue;
            }

            jdbcTemplate.execute(sql);
        }
    }

    protected void dropSchema(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("DROP SCHEMA IF EXISTS sp CASCADE");
    }
}

