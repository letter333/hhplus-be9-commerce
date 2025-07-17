package kr.hhplus.be.server.mock.payment;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.mock.common.Response;
import kr.hhplus.be.server.mock.payment.dto.PaymentRequest;
import kr.hhplus.be.server.mock.payment.dto.PaymentResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Mock Payment API", description = "결제")
public class MockPaymentController {
    @PostMapping("/api/v1/orders/{id}/payments")
    @ApiResponse(
            responseCode = "200",
            description = "주문 결제 성공",
            content = @Content(
                    schema = @Schema(implementation = Response.class),
                    examples = @ExampleObject(
                            name = "주문 결제 예시",
                            value = """
                                {
                                  "code": "OK",
                                  "message": "결제 성공",
                                  "data": {
                                    "id": 1,
                                    "orderId": 1,
                                    "method": "POINT",
                                    "amount": 140000,
                                    "status": "SUCCESS"
                                  }
                                }
                """
                    )
            )
    )
    public Response<PaymentResponse> pay(
            @RequestBody PaymentRequest paymentRequest
    ) {
        return Response.ok("결제 성공", new PaymentResponse(1L, 1L, PaymentResponse.method.POINT, 140000L, PaymentResponse.status.SUCCESS));
    }
}
