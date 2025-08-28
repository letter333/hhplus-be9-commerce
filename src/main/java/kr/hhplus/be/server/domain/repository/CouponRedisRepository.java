package kr.hhplus.be.server.domain.repository;

import java.time.LocalDateTime;

public interface CouponRedisRepository {
    Long incrementIssuedCouponCount(Long couponId);
    Long decrementIssuedCouponCount(Long couponId);
    void addCouponIssueRequestToQueue(Long couponId, Long userId, String couponCode, LocalDateTime expireAt);
    Boolean isAlreadyIssued(Long couponId, Long userId);
    void removeIssuedUser(Long couponId, Long userId);
    Long getIssuedCouponCount(Long couponId);
    void deleteAll();
}
