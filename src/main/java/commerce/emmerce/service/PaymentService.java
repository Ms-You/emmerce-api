package commerce.emmerce.service;

import commerce.emmerce.dto.PaymentDTO;
import commerce.emmerce.repository.PaymentRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepositoryImpl paymentRepository;

    public Mono<PaymentDTO.PaymentResp> details(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(payment -> PaymentDTO.PaymentResp.builder()
                        .paymentId(payment.getPaymentId())
                        .amount(payment.getAmount())
                        .paymentStatus(payment.getPaymentStatus())
                        .paymentMethod(payment.getPaymentMethod())
                        .build());
    }


}
