package kr.hhplus.be.server.domain.service;

import kr.hhplus.be.server.domain.model.Payment;

public interface ExternalPaymentDataPlatformService {
    void sendPaymentInfo(Payment payment);
}
