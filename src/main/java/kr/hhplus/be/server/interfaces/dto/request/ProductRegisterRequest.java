package kr.hhplus.be.server.interfaces.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRegisterRequest(
        @NotBlank(message = "상품 이름은 필수 입력값 입니다.")
        String name,

        @NotBlank(message = "상품 설명은 필수 입력값 입니다.")
        String description,

        @NotNull(message = "상품 가격은 필수 입력값 입니다.")
        Long price,

        @PositiveOrZero(message = "0이상의 숫자만 입력 가능합니다.")
        int stock
) {
}
