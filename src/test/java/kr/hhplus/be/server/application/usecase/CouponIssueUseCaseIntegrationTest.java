package kr.hhplus.be.server.application.usecase;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.application.usecase.dto.command.CouponIssueCommand;
import kr.hhplus.be.server.domain.component.RedissonLockManager;
import kr.hhplus.be.server.domain.model.*;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import kr.hhplus.be.server.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.domain.repository.UserRepository;
import kr.hhplus.be.server.infrastructure.entity.CouponEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private RedissonLockManager redissonLockManager;

    @AfterEach
    void tearDown() {
        userCouponRepository.deleteAll();
        couponRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class CouponIssueTest {
        @Test
        @DisplayName("쿠폰 발급 성공 테스트")
        void 쿠폰_발급_성공() {
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

            Coupon updatedCoupon = couponRepository.findById(savedCoupon.getId()).orElseThrow();
            assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(1);

            Optional<UserCoupon> userCoupon = userCouponRepository.findByCouponIdAndUserId(savedCoupon.getId(), 1L);
            assertThat(userCoupon).isPresent();
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

            // when & then
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
            Coupon savedCoupon = transactionTemplate.execute(status -> {
                Coupon testCoupon = Coupon.builder()
                        .name("선착순 10명 할인 쿠폰")
                        .type(CouponType.FIXED)
                        .issuedQuantity(0)
                        .discountAmount(1000L)
                        .quantity(10)
                        .expiredAt(LocalDateTime.now().plusDays(10))
                        .build();
                return couponRepository.save(testCoupon);
            });



            List<User> testUsers = IntStream.range(1, 101)
                    .mapToObj(i -> userRepository.save(User.builder().name("testUser" + i).phoneNumber("test-phone-" + i).build()))
                    .toList();

            final int threadCount = 100;
            final ExecutorService executorService = Executors.newFixedThreadPool(32);
            final CountDownLatch latch = new CountDownLatch(threadCount);
            final AtomicInteger successCount = new AtomicInteger(0);
            final AtomicInteger failureCount = new AtomicInteger(0);

            // when
            for (User user : testUsers) {
                executorService.submit(() -> {
                    try {
                        CouponIssueCommand command = new CouponIssueCommand(savedCoupon.getId(), user.getId());
                        couponIssueUseCase.execute(command);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            executorService.shutdown();

            // then
            assertThat(successCount.get()).isEqualTo(savedCoupon.getQuantity());
            assertThat(failureCount.get()).isEqualTo(threadCount - savedCoupon.getQuantity());

            List<UserCoupon> issuedCouponCount = userCouponRepository.findByCouponId(savedCoupon.getId());
            assertThat(issuedCouponCount).hasSize(savedCoupon.getQuantity());
        }

    }
}
