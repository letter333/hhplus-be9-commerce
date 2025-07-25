package kr.hhplus.be.server.interfaces.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PointChargeRequest(
        @JsonProperty(required = true)
        @NotNull(message = "충전 금액은 필수입니다.")
        @Min(value = 1, message = "충전 금액은 1원 이상이어야 합니다.")
        Long amount
) {
}
