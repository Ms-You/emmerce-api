package commerce.emmerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class PaymentService {

//    private final PaymentRepositoryImpl paymentRepository;
//
//    /**
//     * 결제 정보 조회
//     * @param paymentId
//     * @return
//     */
//    public Mono<PaymentDTO.PaymentResp> details(Long paymentId) {
//        return paymentRepository.findById(paymentId)
//                .map(payment -> PaymentDTO.PaymentResp.builder()
//                        .paymentId(payment.getPaymentId())
//                        .amount(payment.getAmount())
//                        .paymentStatus(payment.getPaymentStatus())
//                        .paymentMethod(payment.getPaymentMethod())
//                        .build());
//    }


}
