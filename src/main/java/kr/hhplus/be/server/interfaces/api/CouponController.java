package kr.hhplus.be.server.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import kr.hhplus.be.server.application.usecase.CouponGetUseCase;
import kr.hhplus.be.server.application.usecase.CouponIssueUseCase;
import kr.hhplus.be.server.application.usecase.dto.command.CouponIssueCommand;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.interfaces.dto.request.CouponIssueRequest;
import kr.hhplus.be.server.interfaces.dto.response.CouponIssueResponse;
import kr.hhplus.be.server.mock.common.Response;
import kr.hhplus.be.server.mock.coupon.dto.CouponResponse;
import kr.hhplus.be.server.mock.coupon.dto.IssueCouponRequest;
import kr.hhplus.be.server.mock.coupon.dto.IssueCouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestController
@RequiredArgsConstructor
public class CouponController {
    private final CouponIssueUseCase couponIssueUseCase;
    private final CouponGetUseCase couponGetUseCase;

    @Operation(summary = "쿠폰 발급", description = "쿠폰 발급")
    @ApiResponse(
            responseCode = "200",
            description = "쿠폰 발급 성공",
            content = @Content(
                    schema = @Schema(implementation = Response.class),
                    examples = @ExampleObject(
                            name = "쿠폰 발급 예시",
                            value = """
                                {
                                      "code": "OK",
                                      "message": "쿠폰 발급 성공",
                                      "data": {
                                          "id": 3,
                                          "couponName": "특별 할인 쿠폰",
                                          "couponCode": "aaabbbccc",
                                          "couponType": "PERCENTAGE",
                                          "amount": null,
                                          "percentage": 10,
                                          "status": "ISSUED",
                                          "expiredAt": "2025-07-31T00:00:00+09:00",
                                          "createdAt": null
                                      }
                                }
                """
                    )
            )
    )
    @PostMapping("/api/v1/coupons/{id}/issue")
    public CommonResponse<CouponIssueResponse> issue(
            @Parameter(description = "쿠폰 ID", example = "3") @PathVariable Long id,
            @RequestBody CouponIssueRequest couponIssueRequest
    ) {
        CouponIssueCommand command = new CouponIssueCommand(id, couponIssueRequest.getUserId());
        Coupon coupon = couponGetUseCase.execute(command.couponId());
        UserCoupon userCoupon = couponIssueUseCase.execute(command);

        CouponIssueResponse couponIssueResponse = new CouponIssueResponse(
                userCoupon.getId(),
                coupon.getName(),
                userCoupon.getCouponCode(),
                coupon.getType(),
                coupon.getDiscountAmount(),
                coupon.getDiscountPercentage(),
                userCoupon.getStatus(),
                userCoupon.getExpiredAt(),
                userCoupon.getCreatedAt()
        );

        return CommonResponse.ok("쿠폰 발급 성공", couponIssueResponse);
    }
}
