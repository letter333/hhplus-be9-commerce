package kr.hhplus.be.server.interfaces.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import kr.hhplus.be.server.application.usecase.ProductGetListUseCase;
import kr.hhplus.be.server.application.usecase.ProductGetUseCase;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.interfaces.dto.response.ProductListResponse;
import kr.hhplus.be.server.interfaces.dto.response.ProductResponse;
import kr.hhplus.be.server.interfaces.mapper.ProductResponseMapper;
import kr.hhplus.be.server.mock.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductGetUseCase productGetUseCase;
    private final ProductGetListUseCase productGetListUseCase;

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
    public CommonResponse<ProductResponse> getProduct(
            @PathVariable Long id
    ) {
        Product product = productGetUseCase.execute(id);
        ProductResponse productResponse = ProductResponseMapper.toProductResponse(product);
        return CommonResponse.ok(productResponse);
    }

    @GetMapping("/api/v1/products")
    public CommonResponse<ProductListResponse> getProductList() {
        List<Product> products = productGetListUseCase.execute();

        return CommonResponse.ok(new ProductListResponse(products));
    }
}
