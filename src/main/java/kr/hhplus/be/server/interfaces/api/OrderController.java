package kr.hhplus.be.server.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import kr.hhplus.be.server.application.usecase.OrderCreateUseCase;
import kr.hhplus.be.server.application.usecase.dto.command.OrderCreateCommand;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.interfaces.dto.request.OrderCreateRequest;
import kr.hhplus.be.server.interfaces.dto.response.OrderResponse;
import kr.hhplus.be.server.mock.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderCreateUseCase orderCreateUseCase;

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
                                    "recipientNumber": "01012341234",
                                    "createdAt": "2025-07-18T00:57:34.1746101+09:00"
                                  }
                                }
                """
                    )
            )
    )
    @PostMapping(value = "/api/v1/orders")
    public CommonResponse<OrderResponse> create(
            @RequestBody @Valid OrderCreateRequest orderCreateRequest
    ) {
        OrderCreateCommand command = new OrderCreateCommand(
                orderCreateRequest.userId(),
                orderCreateRequest.userCouponId(),
                orderCreateRequest.orderProductList(),
                orderCreateRequest.shippingAddress(),
                orderCreateRequest.recipientNumber()
        );

        Order order = orderCreateUseCase.execute(command);

        OrderResponse orderResponse = new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                null,
                order.getFinalPrice(),
                order.getShippingAddress(),
                order.getRecipientNumber()
        );

        return CommonResponse.ok("주문 성공", orderResponse);
    }
}
