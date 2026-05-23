package com.echoowl.backend.util;

import java.util.UUID;

public final class Cuid {
    private Cuid() {
    }

    public static String create() {
        return "c" + UUID.randomUUID().toString().replace("-", "");
    }
}
