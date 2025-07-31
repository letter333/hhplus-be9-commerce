package kr.hhplus.be.server.infrastructure.external;

import kr.hhplus.be.server.domain.model.Payment;
import kr.hhplus.be.server.domain.service.ExternalPaymentDataPlatformService;
import org.springframework.stereotype.Service;

@Service
public class ExternalPaymentDataPlatformServiceImpl implements ExternalPaymentDataPlatformService {

    @Override
    public void sendPaymentInfo(Payment payment) {
        //외부 데이터플랫폼에 결제 정보 전송
    }
}
