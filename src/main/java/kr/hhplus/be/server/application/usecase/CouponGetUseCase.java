package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponGetUseCase {
    private final CouponRepository couponRepository;

    public Coupon execute(Long id) {
        return couponRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));
    }
}
