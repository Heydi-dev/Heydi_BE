package com.example.heydibe.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean isSuccess,
        int code,
        String message,
        T result
) {
    public static <T> ApiResponse<T> success(int code, String message, T result) {
        return new ApiResponse<>(true, code, message, result);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
