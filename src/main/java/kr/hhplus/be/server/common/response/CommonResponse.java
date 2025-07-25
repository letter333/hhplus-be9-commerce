package kr.hhplus.be.server.common.response;

import org.springframework.http.HttpStatus;

public record CommonResponse<T>(
        int code,
        String message,
        T data
) {

    public CommonResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse<T> ok(String message, T data) {
        return new CommonResponse<>(HttpStatus.OK.value(), message, data);
    }

    public static <T> CommonResponse<T> ok(T data) {
        return new CommonResponse<>(HttpStatus.OK.value(), null, data);
    }
}