package kr.hhplus.be.server.mock.common;

import org.springframework.http.HttpStatus;

public record Response<T>(
    HttpStatus code,
    String message,
    T data
) {

    public Response(HttpStatus code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> ok(String message, T data) {
        return new Response<>(HttpStatus.OK, message, data);
    }
}
