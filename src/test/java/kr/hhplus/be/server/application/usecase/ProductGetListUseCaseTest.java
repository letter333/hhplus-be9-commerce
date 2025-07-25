package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProductGetListUseCaseTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductGetListUseCase productGetListUseCase;

    @Test
    @DisplayName("상품 목록 조회 성공")
    void 상품_목록_조회() {
        // Given
        Product product1 = new Product(1L, "츄르", "고양이 간식", 1000L, 50);
        Product product2 = new Product(2L, "습식 사료", "고양이 밥", 20_000L, 30);
        List<Product> productList = List.of(product1, product2);

        when(productRepository.findAll()).thenReturn(productList);

        // When
        List<Product> result = productGetListUseCase.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(product1);
        assertThat(result.get(1)).isEqualTo(product2);
    }

    @Test
    @DisplayName("저장된 상품이 없으면 빈 리스트 반환")
    void 상품_목록_없을_때_조회() {
        // Given
        when(productRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        List<Product> result = productGetListUseCase.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(productRepository, times(1)).findAll();
        verifyNoMoreInteractions(productRepository);
    }
}