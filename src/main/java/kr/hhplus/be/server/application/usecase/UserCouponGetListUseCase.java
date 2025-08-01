package kr.hhplus.be.server.application.usecase;

import kr.hhplus.be.server.domain.model.UserCoupon;
import kr.hhplus.be.server.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCouponGetListUseCase {
    private final UserCouponRepository userCouponRepository;

    public List<UserCoupon> execute(Long userId) {
        return userCouponRepository.findByUserId(userId);
    }
}
