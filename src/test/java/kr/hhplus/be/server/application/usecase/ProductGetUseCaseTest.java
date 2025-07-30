package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProductGetUseCaseTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductGetUseCase productGetUseCase;

    @Test
    @DisplayName("단일 상품 조회 성공")
    void 상품_조회_성공() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .id(productId)
                .name("츄르")
                .description("고양이 간식")
                .price(1000L)
                .stock(10)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // when
        Product result = productGetUseCase.execute(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("츄르");
        assertThat(result.getDescription()).isEqualTo("고양이 간식");
        assertThat(result.getPrice()).isEqualTo(1000L);
        assertThat(result.getStock()).isEqualTo(10);
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("상품 ID값이 없으면 예외 처리")
    void 상품_ID값_없음() {
        // when & then
        assertThatThrownBy(() -> productGetUseCase.execute(null)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("상품이 존재하지 않으면 예외 처리")
    void shouldThrowExceptionWhenProductDoesNotExist() {
        // given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productGetUseCase.execute(productId)).isInstanceOf(IllegalArgumentException.class);
        verify(productRepository, times(1)).findById(productId);
    }
}