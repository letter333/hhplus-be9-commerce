package kr.hhplus.be.server.interfaces.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @NotBlank(message = "이름은 필수 입력값 입니다.")
        String name,

        @NotBlank(message = "전화번호는 필수 입력값 입니다.")
        String phoneNumber
) {
}
