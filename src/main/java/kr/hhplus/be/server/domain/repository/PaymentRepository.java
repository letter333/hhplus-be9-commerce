package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.Payment;

public interface PaymentRepository {
    Payment save(Payment payment);
}
