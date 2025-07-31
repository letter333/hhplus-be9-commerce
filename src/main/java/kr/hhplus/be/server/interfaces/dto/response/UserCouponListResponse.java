package kr.hhplus.be.server.interfaces.dto.response;

import kr.hhplus.be.server.domain.model.UserCoupon;

import java.util.List;

public record UserCouponListResponse(
        List<UserCoupon> userCouponList
) {
}
