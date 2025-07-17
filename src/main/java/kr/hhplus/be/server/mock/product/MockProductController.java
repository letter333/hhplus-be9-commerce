package kr.hhplus.be.server.mock.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.mock.common.Response;
import kr.hhplus.be.server.mock.product.dto.ProductResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Tag(name = "Mock Product API", description = "단일 상품 조회, 인기 상품 조회")
public class MockProductController {
    @Operation(summary = "단일 상품 조회", description = "상품 ID를 이용해 조회")
    @ApiResponse(
            responseCode = "200",
            description = "상품 조회 성공",
            content = @Content(
                    schema = @Schema(implementation = Response.class),
                    examples = @ExampleObject(
                            name = "단일 상품 조회 예시",
                            value = """
                {
                    "code": "OK",
                    "message": null,
                    "data": {
                        "id": 1,
                        "name": "고양이 화장실",
                        "description": "살 찐 고양이가 쓰기 좋은 아주 큰 화장실",
                        "price": 100000,
                        "stock": 100
                    }
                }
                """
                    )
            )
    )
    @GetMapping("/api/v1/products/{id}")
    public Response<ProductResponse> getProduct(
            @Parameter(description = "상품 ID", example = "1") @PathVariable Long id
    ) {
        return Response.ok(new ProductResponse(1L, "고양이 화장실", "살 찐 고양이가 쓰기 좋은 아주 큰 화장실", 100000L, 100));
    }
}
