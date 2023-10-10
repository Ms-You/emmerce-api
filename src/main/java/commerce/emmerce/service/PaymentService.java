package commerce.emmerce.service;

import commerce.emmerce.domain.Payment;
import commerce.emmerce.dto.PaymentDTO;
import commerce.emmerce.repository.PaymentRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepositoryImpl paymentRepository;


    public Mono<PaymentDTO.PaymentResp> process(PaymentDTO.PaymentReq paymentReq) {
        return paymentRepository.save(Payment.createPayment()
                        .amount(paymentReq.getAmount())
                        .paymentStatus(paymentReq.getPaymentStatus())
                        .paymentMethod(paymentReq.getPaymentMethod())
                        .orderId(paymentReq.getOrderId())
                        .build())
                .map(payment -> PaymentDTO.PaymentResp.builder()
                        .paymentId(payment.getPaymentId())
                        .amount(payment.getAmount())
                        .paymentStatus(payment.getPaymentStatus())
                        .paymentMethod(payment.getPaymentMethod())
                        .build());
    }


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
