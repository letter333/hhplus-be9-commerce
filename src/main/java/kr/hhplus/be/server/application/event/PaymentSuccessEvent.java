package kr.hhplus.be.server.application.event;

import kr.hhplus.be.server.domain.model.Payment;

public record PaymentSuccessEvent(Payment payment) {
}
