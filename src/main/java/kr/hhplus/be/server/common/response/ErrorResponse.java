package kr.hhplus.be.server.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private final int code;
    private final String error;
    private final String message;
}
