package kr.hhplus.be.server.infrastructure.repository.redis;

import kr.hhplus.be.server.application.usecase.dto.ProductSummary;
import kr.hhplus.be.server.domain.model.Order;
import kr.hhplus.be.server.domain.model.OrderProduct;
import kr.hhplus.be.server.domain.model.Product;
import kr.hhplus.be.server.domain.repository.OrderProductRepository;
import kr.hhplus.be.server.domain.repository.ProductRankingRepository;
import kr.hhplus.be.server.domain.repository.ProductRepository;
import kr.hhplus.be.server.infrastructure.mapper.ProductSummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class ProductRankingRedisRepositoryImpl implements ProductRankingRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;

    private static final String RANKING_KEY_PREFIX = "ranking:daily";
    private static final String PRODUCT_SUMMARY_KEY_PREFIX = "product:";
    private static final String PRODUCT_SUMMARY_KEY_SUFFIX = ":summary:v1";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String getDailyRankingKey(LocalDate date) {
        return RANKING_KEY_PREFIX + ":" + DATE_FORMATTER.format(date);
    }

    private String getProductSummaryKey(Long productId) {
        return PRODUCT_SUMMARY_KEY_PREFIX + productId + PRODUCT_SUMMARY_KEY_SUFFIX;
    }

    @Override
    public void updateRanking(Order order) {
        String key = getDailyRankingKey(LocalDate.now());
        List<OrderProduct> orderProducts = orderProductRepository.findByOrderId(order.getId());

        if(orderProducts.isEmpty()) {
            throw new IllegalArgumentException("잘못된 주문 정보 입니다.");
        }

        orderProducts.forEach(orderProduct -> {
            redisTemplate.opsForZSet().incrementScore(
                    key,
                    orderProduct.getProductId().toString(),
                    orderProduct.getQuantity()
            );
        });
        redisTemplate.expire(key, 10, TimeUnit.DAYS);
    }

    @Override
    public List<ProductSummary> getRanking(LocalDate startDate, LocalDate endDate, Long limit) {
        if (ChronoUnit.DAYS.between(startDate, endDate) >= 7) {
            throw new IllegalArgumentException("조회 기간은 최대 7일입니다.");
        }

        List<String> dailyKeys = Stream.iterate(startDate, date -> !date.isAfter(endDate), date -> date.plusDays(1))
                .map(this::getDailyRankingKey)
                .toList();

        if (dailyKeys.isEmpty()) {
            return Collections.emptyList();
        }

        String sourceKey;
        boolean isTempKey = false;

        if (dailyKeys.size() > 1) {
            sourceKey = "ranking:union:" + UUID.randomUUID();
            isTempKey = true;
            redisTemplate.opsForZSet().unionAndStore(dailyKeys.get(0), dailyKeys.subList(1, dailyKeys.size()), sourceKey);
            redisTemplate.expire(sourceKey, 1, TimeUnit.MINUTES);
        } else {
            sourceKey = dailyKeys.get(0);
        }

        Set<Object> productIdsAsObjects = redisTemplate.opsForZSet().reverseRange(sourceKey, 0, limit - 1);

        if (isTempKey) {
            redisTemplate.delete(sourceKey);
        }

        if (productIdsAsObjects == null || productIdsAsObjects.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = productIdsAsObjects.stream()
                .map(id -> Long.valueOf(String.valueOf(id)))
                .toList();

        List<ProductSummary> summaries = fetchProductSummaries(productIds);

        return summaries.stream()
                .sorted(Comparator.comparing(productSummary -> productIds.indexOf(productSummary.id())))
                .toList();

    }

    private List<ProductSummary> fetchProductSummaries(List<Long> productIds) {
        List<String> summaryKeys = productIds.stream()
                .map(this::getProductSummaryKey)
                .toList();

        List<Object> cachedObjects = redisTemplate.opsForValue().multiGet(summaryKeys);

        List<ProductSummary> resultSummaries = new ArrayList<>();
        List<Long> missedIds = new ArrayList<>();
        Map<Long, ProductSummary> cachedSummariesMap = new HashMap<>();

        for(int i = 0; i < productIds.size(); i++) {
            if(cachedObjects.get(i) != null) {
                ProductSummary summary = (ProductSummary) cachedObjects.get(i);
                resultSummaries.add(summary);
                cachedSummariesMap.put(summary.id(), summary);
            } else {
                missedIds.add(productIds.get(i));
            }
        }

        if(!missedIds.isEmpty()) {
            List<Product> foundProducts = productRepository.findAllByIdIn(missedIds);

            Map<String, Object> newCacheEntries = new HashMap<>();
            for(Product product : foundProducts) {
                ProductSummary newSummary = ProductSummaryMapper.toProductSummary(product);
                resultSummaries.add(newSummary);
                newCacheEntries.put(getProductSummaryKey(product.getId()), newSummary);
            }

            if(!newCacheEntries.isEmpty()) {
                redisTemplate.opsForValue().multiSet(newCacheEntries);
                newCacheEntries.keySet().forEach(key -> {
                    long ttl = ThreadLocalRandom.current().nextLong(5, 10);
                    redisTemplate.expire(key, ttl, TimeUnit.MINUTES);
                });
            }
        }

        return resultSummaries;
    }
}
