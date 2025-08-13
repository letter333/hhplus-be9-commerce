package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.domain.model.PointHistory;
import kr.hhplus.be.server.domain.model.PointHistoryType;
import kr.hhplus.be.server.domain.model.User;
import kr.hhplus.be.server.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.repository.PointRepository;
import kr.hhplus.be.server.domain.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class PointChargeUseCaseIntegrationTest {
    @Autowired
    private PointChargeUseCase pointChargeUseCase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    private User testUser;
    private Point testPoint;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                        .name("오세룡")
                        .phoneNumber("010-1234-1234")
                        .build());

        testPoint = pointRepository.save(Point.builder()
                        .userId(testUser.getId())
                        .balance(0L)
                        .build());
    }

    @AfterEach
    void tearDown() {
        pointRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("포인트 충전 테스트")
    class PointChargeTest {
        @Test
        @DisplayName("포인트 충전 성공")
        void 포인트_충전() {
            // given
            Long userId = testUser.getId();
            Long initialBalance = testPoint.getBalance();
            Long chargeAmount = 10000L;

            // when
            Point result = pointChargeUseCase.execute(userId, chargeAmount);

            // then
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getBalance()).isEqualTo(initialBalance + chargeAmount);

            Point savedPoint = pointRepository.findByUserId(userId).orElseThrow();
            assertThat(savedPoint.getBalance()).isEqualTo(initialBalance + chargeAmount);
        }

        @Test
        @DisplayName("충전 금액이 null일 때 예외 발생")
        void 충전_금액_null() {
            // given
            Long userId = testUser.getId();
            Long chargeAmount = null;

            // when & then
            assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("충전 금액이 0 이하일 때 예외 발생")
        void 충전_금액_0이하() {
            // given
            Long userId = testUser.getId();
            Long chargeAmount = 0L;

            // when & then
            assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("1회 최대 충전 가능 금액을 초과할 때 예외 발생")
        void 최대_충전금액_초과() {
            // given
            Long userId = testUser.getId();
            Long chargeAmount = 1_000_001L;

            // when & then
            assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("충전 후 최대 보유 가능 포인트를 초과할 때 예외 발생")
        void 최대_보유포인트_초과_예외() {
            // given
            Long userId = testUser.getId();
            Long chargeAmount = 10_000_001L;

            // when & then
            assertThatThrownBy(() -> pointChargeUseCase.execute(userId, chargeAmount))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("포인트 충전 동시성 제어 테스트")
    class ConcurrencyControlTest {
        @Test
        @DisplayName("여러 번의 포인트 충전 요청이 동시에 발생해도 잔액이 정상 증가")
        void 동시성_테스트() throws InterruptedException {
            // given
            final int threadCount = 5;
            final long chargeAmount = 100L;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            // when
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        pointChargeUseCase.execute(testUser.getId(), chargeAmount);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            // then
            Point finalPoint = pointRepository.findByUserId(testUser.getId()).orElseThrow();
            long expectedBalance = threadCount * chargeAmount;
            assertThat(finalPoint.getBalance()).isEqualTo(expectedBalance);

            List<PointHistory> histories = pointHistoryRepository.findByUserId(testUser.getId());
            assertThat(histories).hasSize(threadCount);
            assertThat(histories).allMatch(history ->
                    history.getType() == PointHistoryType.CHARGE &&
                            history.getAmount().equals(chargeAmount)
            );

        }

        @Test
        @DisplayName("예외 상황에서의 동시성 테스트")
        void 예외상황_동시성_테스트() throws InterruptedException {
            // given
            final int successThreadCount = 5;
            final int failureThreadCount = 5;
            final long validChargeAmount = 1000L;
            final long invalidChargeAmount = -100L;

            ExecutorService executorService = Executors.newFixedThreadPool(successThreadCount + failureThreadCount);
            CountDownLatch latch = new CountDownLatch(successThreadCount + failureThreadCount);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // when
            for (int i = 0; i < successThreadCount; i++) {
                executorService.submit(() -> {
                    try {
                        pointChargeUseCase.execute(testUser.getId(), validChargeAmount);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            for (int i = 0; i < failureThreadCount; i++) {
                executorService.submit(() -> {
                    try {
                        pointChargeUseCase.execute(testUser.getId(), invalidChargeAmount);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executorService.shutdown();

            // then
            assertThat(successCount.get()).isEqualTo(successThreadCount);
            assertThat(failureCount.get()).isEqualTo(failureThreadCount);

            Point finalPoint = pointRepository.findByUserId(testUser.getId()).orElseThrow();
            assertThat(finalPoint.getBalance()).isEqualTo(successThreadCount * validChargeAmount);
        }
    }

    @Test
    @DisplayName("데이터베이스 격리 수준 검증")
    void 데이터베이스_격리수준_검증() throws InterruptedException {
        // given
        final int threadCount = 5;
        final long chargeAmount = 500L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Long> observedBalances = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    Point result = pointChargeUseCase.execute(testUser.getId(), chargeAmount);
                    observedBalances.add(result.getBalance());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // then
        Collections.sort(observedBalances);
        Set<Long> uniqueBalances = new HashSet<>(observedBalances);

        assertThat(uniqueBalances).hasSize(threadCount);
        assertThat(observedBalances.get(0)).isEqualTo(chargeAmount);
        assertThat(observedBalances.get(threadCount - 1)).isEqualTo(threadCount * chargeAmount);

        for (int i = 1; i < observedBalances.size(); i++) {
            long diff = observedBalances.get(i) - observedBalances.get(i - 1);
            assertThat(diff).isEqualTo(chargeAmount);
        }
    }

}
