package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.application.usecase.dto.command.CouponRegisterCommand;
import kr.hhplus.be.server.domain.model.Coupon;
import kr.hhplus.be.server.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponRegisterUseCase {
    private final CouponRepository couponRepository;

    public Coupon execute(CouponRegisterCommand command) {
        Coupon coupon = Coupon.builder()
                .name(command.name())
                .type(command.type())
                .discountAmount(command.discountAmount())
                .quantity(command.quantity())
                .expiredAt(command.expiredAt())
                .build();

        return couponRepository.save(coupon);
    }
}
