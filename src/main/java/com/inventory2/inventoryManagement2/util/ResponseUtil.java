package com.inventory2.inventoryManagement2.util;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public final class ResponseUtil {

    private ResponseUtil() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated");
    }

    public static ResponseEntity<Map<String, Object>> success(String message) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);

        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<Map<String, Object>> success(
            String message,
            Object data) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}