package kr.hhplus.be.server.application.usecase;


import kr.hhplus.be.server.application.usecase.dto.command.CouponIssueCommand;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.CouponRedisRepository;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import kr.hhplus.be.server.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Testcontainers
public class CouponIssueUseCaseIntegrationTest {

    @Autowired
    private CouponIssueUseCase couponIssueUseCase;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRedisRepository couponRedisRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        userCouponRepository.deleteAll();
        couponRepository.deleteAll();
        userRepository.deleteAll();
        couponRedisRepository.deleteAll();
    }

    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class CouponIssueTest {
        @Test
        @DisplayName("쿠폰 발급 성공 테스트")
        void 쿠폰_발급_성공() throws InterruptedException {
            // given
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(30).truncatedTo(ChronoUnit.MICROS);
            Coupon coupon = Coupon.builder()
                    .name("할인 쿠폰")
                    .type(CouponType.FIXED)
                    .discountAmount(1000L)
                    .quantity(100)
                    .issuedQuantity(0)
                    .expiredAt(expiredAt)
                    .createdAt(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
                    .build();

            Coupon savedCoupon = couponRepository.save(coupon);
            CouponIssueCommand command = new CouponIssueCommand(savedCoupon.getId(), 1L);

            // when
            UserCoupon result = couponIssueUseCase.execute(command);

            // when
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getCouponId()).isEqualTo(savedCoupon.getId());
            assertThat(result.getStatus()).isEqualTo(UserCouponStatus.ISSUED);
            assertThat(result.getCouponCode()).isNotNull();
            assertThat(result.getExpiredAt()).isEqualTo(expiredAt);

            Long issuedCount = couponRedisRepository.getIssuedCouponCount(savedCoupon.getId());
            assertThat(issuedCount).isEqualTo(1);
            assertThat(couponRedisRepository.isAlreadyIssued(savedCoupon.getId(), 1L)).isTrue();

        }

        @Test
        @DisplayName("존재하지 않는 쿠폰으로 발급 시도 시 예외 발생")
        void 존재하지_않는_쿠폰_발급() {
            // given
            CouponIssueCommand command = new CouponIssueCommand(999L, 1L);

            // when & then
            assertThatThrownBy(() -> couponIssueUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("이미 발급받은 쿠폰으로 중복 발급 시도 시 예외 발생")
        void 쿠폰_중복_발급() {
            // given
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);
            Coupon coupon = Coupon.builder()
                    .name("할인 쿠폰")
                    .type(CouponType.FIXED)
                    .discountAmount(1000L)
                    .quantity(100)
                    .issuedQuantity(0)
                    .expiredAt(expiredAt)
                    .createdAt(LocalDateTime.now())
                    .build();

            Coupon savedCoupon = couponRepository.save(coupon);

            UserCoupon existingUserCoupon = UserCoupon.builder()
                    .userId(1L)
                    .couponId(savedCoupon.getId())
                    .couponCode("ABCD-1234-5678-0123")
                    .status(UserCouponStatus.ISSUED)
                    .expiredAt(expiredAt)
                    .createdAt(LocalDateTime.now())
                    .build();
            userCouponRepository.save(existingUserCoupon);

            CouponIssueCommand command = new CouponIssueCommand(savedCoupon.getId(), 1L);

            // when
            couponIssueUseCase.execute(command);

            // then
            assertThatThrownBy(() -> couponIssueUseCase.execute(command))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("쿠폰 발급 동시성 테스트")
    class ConcurrencyControlTest {
        @Test
        @DisplayName("동시에 100명의 유저가 10개 한정 쿠폰 발급 요청 시 10명만 성공해야 한다.")
        void 동시성_쿠폰_발급_테스트() throws InterruptedException {
            // given
            int userCount = 100;
            int couponQuantity = 10;
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            CountDownLatch latch = new CountDownLatch(userCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            Coupon savedCoupon = transactionTemplate.execute(status -> {
                Coupon testCoupon = Coupon.builder()
                        .name("선착순 10명 할인 쿠폰")
                        .type(CouponType.FIXED)
                        .issuedQuantity(0)
                        .discountAmount(1000L)
                        .quantity(couponQuantity)
                        .expiredAt(LocalDateTime.now().plusDays(10))
                        .build();
                return couponRepository.save(testCoupon);
            });

            // when
            for (int i = 0; i < userCount; i++) {
                final long userId = i + 1;
                executorService.submit(() -> {
                    try {
                        CouponIssueCommand command = new CouponIssueCommand(savedCoupon.getId(), userId);
                        couponIssueUseCase.execute(command);
                        successCount.getAndIncrement();
                    } catch (IllegalArgumentException e) {
                        failCount.getAndIncrement();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            assertThat(successCount.get()).isEqualTo(couponQuantity);
            assertThat(failCount.get()).isEqualTo(userCount - couponQuantity);

            Long issuedCount = couponRedisRepository.getIssuedCouponCount(savedCoupon.getId());
            assertThat(issuedCount).isEqualTo(couponQuantity);
        }
    }
}
