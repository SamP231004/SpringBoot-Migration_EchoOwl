package com.echoowl.backend.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class DotenvLoader {
    private DotenvLoader() {
    }

    public static void load() {
        loadFile(Path.of(".env"));
        loadFile(Path.of("..", ".env"));
    }

    private static void loadFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                parseLine(line);
            }
        } catch (IOException ignored) {
        }
    }

    private static void parseLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isBlank() || trimmed.startsWith("#")) {
            return;
        }

        int separator = trimmed.indexOf('=');
        if (separator <= 0) {
            return;
        }

        String key = trimmed.substring(0, separator).trim();
        String value = stripComment(trimmed.substring(separator + 1).trim());
        value = stripWrappingQuotes(value);

        if (!key.isBlank() && System.getProperty(key) == null && System.getenv(key) == null) {
            System.setProperty(key, value);
        }
    }

    private static String stripComment(String value) {
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;

        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
            } else if (current == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
            } else if (current == '#' && !inSingleQuotes && !inDoubleQuotes) {
                return value.substring(0, i).trim();
            }
        }

        return value;
    }

    private static String stripWrappingQuotes(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
