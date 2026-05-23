package com.echoowl.backend.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DataSourceConfig {
    @Bean
    DataSource dataSource(@Value("${spring.datasource.url:}") String configuredUrl) {
        JdbcConnection connection = normalizeJdbcUrl(configuredUrl);
        if (connection.url() == null || connection.url().isBlank()) {
            throw new IllegalStateException("DATABASE_URL or JDBC_DATABASE_URL must be configured");
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(connection.url());
        dataSource.setDriverClassName("org.postgresql.Driver");
        if (connection.username() != null) {
            dataSource.setUsername(connection.username());
        }
        if (connection.password() != null) {
            dataSource.setPassword(connection.password());
        }
        return dataSource;
    }

    private JdbcConnection normalizeJdbcUrl(String url) {
        if (url == null) {
            return new JdbcConnection(null, null, null);
        }
        String trimmed = stripWrappingQuotes(url.trim());
        if (trimmed.startsWith("jdbc:postgresql://")) {
            return new JdbcConnection(trimmed, null, null);
        }
        if (trimmed.startsWith("postgresql://")) {
            return parsePostgresUrl(trimmed);
        }
        if (trimmed.startsWith("postgres://")) {
            return parsePostgresUrl("postgresql://" + trimmed.substring("postgres://".length()));
        }
        return new JdbcConnection(trimmed, null, null);
    }

    private JdbcConnection parsePostgresUrl(String url) {
        URI uri = URI.create(url);
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://").append(uri.getHost());
        if (uri.getPort() > 0) {
            jdbcUrl.append(":").append(uri.getPort());
        }
        jdbcUrl.append(uri.getRawPath());
        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            jdbcUrl.append("?").append(uri.getRawQuery());
        }

        String username = null;
        String password = null;
        String userInfo = uri.getRawUserInfo();
        if (userInfo != null) {
            int separator = userInfo.indexOf(':');
            if (separator >= 0) {
                username = decode(userInfo.substring(0, separator));
                password = decode(userInfo.substring(separator + 1));
            } else {
                username = decode(userInfo);
            }
        }

        return new JdbcConnection(jdbcUrl.toString(), username, password);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String stripWrappingQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private record JdbcConnection(String url, String username, String password) {
    }
}
