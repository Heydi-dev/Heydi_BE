package com.example.heydibe.file.util;

import java.util.UUID;

public class UUIDUtil {
    private UUIDUtil() {}

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}

