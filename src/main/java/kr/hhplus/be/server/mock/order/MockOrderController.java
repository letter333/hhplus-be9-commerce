package kr.hhplus.be.server.mock.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.mock.common.Response;
import kr.hhplus.be.server.mock.order.dto.CreateOrderRequest;
import kr.hhplus.be.server.mock.order.dto.CreateOrderResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@Tag(name = "Mock Order API", description = "상품 주문")
public class MockOrderController {
    @Operation(summary = "상품 주문", description = "상품 목록들을 받아 주문 생성")
    @ApiResponse(
            responseCode = "200",
            description = "상품 주문 성공",
            content = @Content(
                    schema = @Schema(implementation = Response.class),
                    examples = @ExampleObject(
                            name = "상품 주문 예시",
                            value = """
                                {
                                  "code": "OK",
                                  "message": "주문 성공",
                                  "data": {
                                    "id": 1,
                                    "userCouponId": 1,
                                    "status": "PENDING",
                                    "totalAmount": 140000,
                                    "discountAmount": 10000,
                                    "finalAmount": 130000,
                                    "shippingAddress1": "기본 주소",
                                    "shippingAddress2": "상세 주소",
                                    "shippingZipCode": "123456",
                                    "createdAt": "2025-07-18T00:57:34.1746101+09:00"
                                  }
                                }
                """
                    )
            )
    )
    @PostMapping(value = "/api/v1/orders")
    public Response<CreateOrderResponse> create(
            @RequestBody CreateOrderRequest createOrderRequest
    ) {
        return Response.ok("주문 성공", new CreateOrderResponse(
                1L,
                1L,
                CreateOrderResponse.status.PENDING,
                140000L,
                10000L,
                130000L,
                "기본 주소",
                "상세 주소",
                "123456",
                ZonedDateTime.now()
        ));
    }
}
