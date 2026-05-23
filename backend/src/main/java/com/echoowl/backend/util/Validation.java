package com.echoowl.backend.util;

import java.util.regex.Pattern;

public final class Validation {
    private static final Pattern CATEGORY_NAME = Pattern.compile("^[a-zA-Z0-9_-]{1,30}$");
    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private Validation() {
    }

    public static void categoryName(String name) {
        if (name == null || !CATEGORY_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid category name");
        }
    }

    public static int hexColorToInt(String color) {
        if (color == null || !HEX_COLOR.matcher(color).matches()) {
            throw new IllegalArgumentException("Invalid color");
        }
        return Integer.parseInt(color.substring(1), 16);
    }
}
