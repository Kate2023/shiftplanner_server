package com.example.shiftplanner_server.repositories;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
public abstract class PostgresRepositoryTestBase {


    protected void createTablesFromSql(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS sp");
        jdbcTemplate.execute("SET search_path TO sp");

        String script;
        try {
            script = Files.readString(Path.of("database", "table.sql"));
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read database/table.sql", ex);
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

