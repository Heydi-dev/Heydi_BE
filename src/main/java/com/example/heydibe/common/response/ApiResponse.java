package com.example.heydibe.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T result;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "200", message, data);
    }

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>(true, String.valueOf(code), message, data);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return fail(String.valueOf(code), message);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return fail(code, message);
    }
}
