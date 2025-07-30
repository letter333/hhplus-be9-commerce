package kr.hhplus.be.server.interfaces.api;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import kr.hhplus.be.server.application.usecase.PaymentFailureRecordUseCase;
import kr.hhplus.be.server.application.usecase.PaymentProcessUseCase;
import kr.hhplus.be.server.application.usecase.dto.command.PaymentFailureCommand;
import kr.hhplus.be.server.application.usecase.dto.command.PaymentProcessCommand;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.interfaces.dto.request.PaymentProcessRequest;
import kr.hhplus.be.server.interfaces.dto.response.PaymentResponse;
import kr.hhplus.be.server.interfaces.mapper.PaymentResponseMapper;
import kr.hhplus.be.server.mock.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentProcessUseCase paymentProcessUseCase;
    private final PaymentFailureRecordUseCase paymentFailureRecordUseCase;

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
    @PostMapping("/api/v1/orders/{id}/payments")
    public CommonResponse<PaymentResponse> payment(
            @PathVariable Long id,
            @RequestBody PaymentProcessRequest request
    ) {
        try {
            PaymentProcessCommand command = new PaymentProcessCommand(
                    id,
                    request.orderId(),
                    request.paymentMethod()
            );
            Payment payment = paymentProcessUseCase.execute(command);

            PaymentResponse paymentResponse = PaymentResponseMapper.toPaymentResponse(payment);

            return CommonResponse.ok("결제 성공", paymentResponse);
        } catch(IllegalArgumentException e) {
            PaymentFailureCommand command = new PaymentFailureCommand(
                    id,
                    request.orderId(),
                    request.paymentMethod()
            );

            Payment failedPayment = paymentFailureRecordUseCase.execute(command);
            PaymentResponse response = PaymentResponseMapper.toPaymentResponse(failedPayment);

            return CommonResponse.fail(HttpStatus.BAD_REQUEST.value(), "결제에 실패했습니다.", response);
        }

    }
}
