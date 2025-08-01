package kr.hhplus.be.server.interfaces.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.domain.model.PaymentMethod;

public record PaymentProcessRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        Long orderId,

        @NotNull(message = "결제 방법은 필수입니다.")
        PaymentMethod paymentMethod
) {
}
