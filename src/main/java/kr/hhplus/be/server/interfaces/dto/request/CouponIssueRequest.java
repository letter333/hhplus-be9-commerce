package kr.hhplus.be.server.interfaces.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CouponIssueRequest {
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;
}
