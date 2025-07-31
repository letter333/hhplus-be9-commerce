package kr.hhplus.be.server.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import kr.hhplus.be.server.application.usecase.PointChargeUseCase;
import kr.hhplus.be.server.application.usecase.PointGetUseCase;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.interfaces.dto.request.PointChargeRequest;
import kr.hhplus.be.server.interfaces.dto.response.PointResponse;
import kr.hhplus.be.server.interfaces.mapper.PointResponseMapper;
import kr.hhplus.be.server.mock.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PointController {
    private final PointChargeUseCase pointChargeUseCase;
    private final PointGetUseCase pointGetUseCase;

    @Operation(summary = "포인트 조회", description = "사용자 ID를 이용해 포인트 조회")
    @ApiResponse(
            responseCode = "200",
            description = "충전 성공",
            content = @Content(
                    schema = @Schema(implementation = Response.class),
                    examples = @ExampleObject(
                            name = "포인트 조회 예시",
                            value = """
                {
                    "code": "OK",
                    "message": "포인트 조회 성공",
                    "data": {
                        "id": 1,
                        "amount": 100000
                    }
                }
                """
                    )
            )
    )
    @GetMapping(value = "/api/v1/users/{id}/points")
    public CommonResponse<PointResponse> getPoint(@Parameter(description = "사용자 ID", example = "1") @PathVariable Long id) {
        Point point = pointGetUseCase.execute(id);
        PointResponse pointResponse = PointResponseMapper.toPointResponse(point);

        return CommonResponse.ok("포인트 조회 성공", pointResponse);
    }

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
        PointResponse pointResponse = PointResponseMapper.toPointResponse(chargedPoint);

        return CommonResponse.ok("포인트 충전 완료", pointResponse);
    }
}
