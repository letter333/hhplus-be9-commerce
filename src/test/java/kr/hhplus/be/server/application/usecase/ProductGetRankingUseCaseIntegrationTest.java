package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.ProductSummary;
import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.model.User;
import kr.hhplus.be.server.domain.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ProductGetRankingUseCaseIntegrationTest {
    @Autowired
    private ProductGetRankingUseCase productGetRankingUseCase;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private Product product1;
    private Product product2;
    private Product product3;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("테스트 사용자")
                .phoneNumber("010-1234-5678")
                .build();
        userRepository.save(user);

        product1 = Product.builder()
                .name("상품1")
                .description("설명1")
                .price(1000L)
                .stock(100)
                .build();
        product2 = Product.builder()
                .name("상품2")
                .description("설명2")
                .price(2000L)
                .stock(100)
                .build();
        product3 = Product.builder()
                .name("상품3")
                .description("설명3")
                .price(3000L)
                .stock(100)
                .build();

        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적인 기간 범위로 랭킹을 조회")
    void 랭킹_조회() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(3);
        LocalDate endDate = LocalDate.now();
        Long limit = 5L;

        // when
        List<ProductSummary> result = productGetRankingUseCase.execute(startDate, endDate, limit);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSizeLessThanOrEqualTo(limit.intValue());
    }

    @Test
    @DisplayName("7일을 초과하는 기간으로 조회하면 예외가 발생")
    void 조회기간_7일_초과() {
        // given
        LocalDate startDate = LocalDate.now().minusDays(8);
        LocalDate endDate = LocalDate.now();
        Long limit = 5L;

        // when & then
        assertThatThrownBy(() -> productGetRankingUseCase.execute(startDate, endDate, limit))
                .isInstanceOf(IllegalArgumentException.class);
    }
}