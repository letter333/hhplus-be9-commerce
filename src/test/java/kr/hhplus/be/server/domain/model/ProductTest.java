package kr.hhplus.be.server.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Product 도메인 테스트")
class ProductTest {
    @Nested
    @DisplayName("재고 감소 시")
    class DecreaseStock {
        @Test
        @DisplayName("재고 감소 성공")
        void 재고_감소() {
            //given
            Product product = new Product(1L, "츄르", "고양이 간식", 1000L, 50);
            int decreaseAmount = 10;
            int expectedStock = 40;

            //when
            product.decreaseStock(decreaseAmount);

            //then
            assertThat(product.getStock()).isEqualTo(expectedStock);
        }

        @ParameterizedTest()
        @ValueSource(ints = {0, -1, 51})
        @DisplayName("유효하지 않은 입력값이면 예외 처리")
        void 유효하지_않은_입력값(int decreaseAmount) {
            //given
            Product product = new Product(1L, "츄르", "고양이 간식", 1000L, 50);

            //when & then
            assertThatThrownBy(() -> product.decreaseStock(decreaseAmount))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("재고 증가 시")
    class IncreaseStock {
        @Test
        @DisplayName("재고 증가 성공")
        void 재고_증가() {
            //given
            Product product = new Product(1L, "츄르", "고양이 간식", 1000L, 50);
            int increaseAmount = 50;
            int expectedStock = 100;

            //when
            product.increaseStock(increaseAmount);

            //then
            assertThat(product.getStock()).isEqualTo(expectedStock);
        }

        @ParameterizedTest()
        @ValueSource(ints = {0, -1})
        @DisplayName("유효하지 않은 입력값이면 예외 처리")
        void 유효하지_않은_입력값(int increaseAmount) {
            //given
            Product product = new Product(1L, "츄르", "고양이 간식", 1000L, 50);

            //when & then
            assertThatThrownBy(() -> product.increaseStock(increaseAmount))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}