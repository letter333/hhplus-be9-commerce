package kr.hhplus.be.server.mock.point;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.mock.point.dto.ChargePointRequest;
import kr.hhplus.be.server.mock.point.dto.ChargePointResponse;
import kr.hhplus.be.server.mock.point.dto.PointResponse;
import kr.hhplus.be.server.mock.common.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Mock Point API", description = "Point 충전, 조회")
public class MockPointController {
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
    public Response<PointResponse> getPoint(@Parameter(description = "사용자 ID", example = "1") @PathVariable Long id) {
        return Response.ok("포인트 조회 성공", new PointResponse(1L, 100000L));
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
    @PostMapping(value = "/api/v1/users/{id}/points")
    public Response<ChargePointResponse> charge(
            @Parameter(description = "사용자 ID", required = true, example = "1") @PathVariable Long id,
            @RequestBody ChargePointRequest chargePointRequest
    ) {
        return Response.ok("포인트 충전 완료", new ChargePointResponse(1L, 100000L));
    }
}
