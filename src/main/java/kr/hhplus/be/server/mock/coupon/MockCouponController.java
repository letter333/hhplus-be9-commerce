package kr.hhplus.be.server.mock.coupon;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.mock.common.Response;
import kr.hhplus.be.server.mock.coupon.dto.CouponResponse;
import kr.hhplus.be.server.mock.coupon.dto.IssueCouponRequest;
import kr.hhplus.be.server.mock.coupon.dto.IssueCouponResponse;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@Tag(name = "Mock Coupon API", description = "Coupon 발급, 조회")
public class MockCouponController {
    @Operation(summary = "쿠폰 조회", description = "사용자 ID를 이용해 쿠폰 조회")
    @ApiResponse(
            responseCode = "200",
            description = "쿠폰 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = Response.class),
                    examples = @ExampleObject(
                            name = "쿠폰 조회 예시",
                            value = """
                                    {
                                      "code": "OK",
                                      "message": null,
                                      "data": [
                                        {
                                          "id": 1,
                                          "couponName": "신규 가입 쿠폰",
                                          "couponCode": "abcdef",
                                          "couponType": "AMOUNT",
                                          "amount": 10000,
                                          "percentage": null,
                                          "status": "ISSUED",
                                          "usedAt": null,
                                          "expiredAt": "2025-07-31T00:00:00+09:00",
                                          "createdAt": null
                                        },
                                        {
                                          "id": 2,
                                          "couponName": "선착순 할인 쿠폰",
                                          "couponCode": "1234512345",
                                          "couponType": "AMOUNT",
                                          "amount": 5000,
                                          "percentage": null,
                                          "status": "ISSUED",
                                          "usedAt": null,
                                          "expiredAt": "2025-07-31T00:00:00+09:00",
                                          "createdAt": null
                                        }
                                      ]
                                    }
                """
                    )
            )
    )
    @GetMapping(value = "/api/v1/users/{id}/coupons")
    public Response<List<CouponResponse>> getCouponList(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long id
    ) {
        List<CouponResponse> couponResponses = new ArrayList<>();

        couponResponses.add(
                new CouponResponse(
                        1L,
                        "신규 가입 쿠폰",
                        "abcdef", CouponResponse.type.AMOUNT,
                        10000L,
                        null,
                        CouponResponse.status.ISSUED,
                        null,
                        ZonedDateTime.of(2025, 7, 31, 0, 0, 0, 0, ZoneId.of("Asia/Seoul")),
                        null)
        );

        couponResponses.add(
                new CouponResponse(
                        2L,
                        "선착순 할인 쿠폰",
                        "1234512345", CouponResponse.type.AMOUNT,
                        5000L,
                        null,
                        CouponResponse.status.ISSUED,
                        null,
                        ZonedDateTime.of(2025, 7, 31, 0, 0, 0, 0, ZoneId.of("Asia/Seoul")),
                        null)
        );

        return Response.ok(couponResponses);
    }

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
    public Response<IssueCouponResponse> issue(
            @Parameter(description = "쿠폰 ID", example = "3") @PathVariable Long id,
            @RequestBody IssueCouponRequest issueCouponRequest
    ) {
        IssueCouponResponse coupon = new IssueCouponResponse(
                3L,
                "특별 할인 쿠폰",
                "aaabbbccc", CouponResponse.type.PERCENTAGE,
                null,
                10L,
                CouponResponse.status.ISSUED,
                ZonedDateTime.of(2025, 7, 31, 0, 0, 0, 0, ZoneId.of("Asia/Seoul")),
                null);

        return Response.ok("쿠폰 발급 성공", coupon);
    }
}
