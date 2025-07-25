package kr.hhplus.be.server.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import kr.hhplus.be.server.application.usecase.PointChargeUseCase;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.interfaces.dto.request.PointChargeRequest;
import kr.hhplus.be.server.mock.common.Response;
import kr.hhplus.be.server.mock.point.dto.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PointController {
    private final PointChargeUseCase pointChargeUseCase;


    @Operation(summary = "잔액 충전", description = "사용자 ID를 이용해 잔액 충전")
    @ApiResponse(
            responseCode = "200",
            description = "충전 성공",
            content = @Content(
                    schema = @Schema(implementation = Response.class),
                    examples = @ExampleObject(
                            name = "포인트 충전 예시",
                            value = """
                {
                    "code": "OK",
                    "message": "포인트 충전 완료",
                    "data": {
                        "id": 1,
                        "amount": 100000
                    }
                }
                """
                    )
            )
    )
    @PostMapping("/api/v1/users/{id}/points")
    public CommonResponse<PointResponse> chargePoint(@PathVariable Long id, @RequestBody @Valid PointChargeRequest pointChargeRequest) {
        Point chargedPoint = pointChargeUseCase.execute(id, pointChargeRequest.amount());

        return CommonResponse.ok("포인트 충전 완료", new PointResponse(chargedPoint.getId(), chargedPoint.getAmount()));
    }
}
