package kr.hhplus.be.server.domain.repository;

import kr.hhplus.be.server.domain.model.Payment;

import java.util.List;

public interface PaymentRepository {
    Payment save(Payment payment);
    List<Payment> saveAll(List<Payment> payments);
}
