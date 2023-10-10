package commerce.emmerce.repository;

import commerce.emmerce.domain.Payment;
import commerce.emmerce.domain.PaymentMethod;
import commerce.emmerce.domain.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Repository
public class PaymentRepositoryImpl {

    private final DatabaseClient databaseClient;


    public Mono<Payment> save(Payment payment) {
        String query = """
                insert into payment (amount, payment_status, payment_method, order_id) 
                values (:amount, :paymentStatus, :paymentMethod, :orderId)
                returning *
                """;

        return databaseClient.sql(query)
                .bind("amount", payment.getAmount())
                .bind("paymentStatus", payment.getPaymentStatus().name())
                .bind("paymentMethod", payment.getPaymentMethod().name())
                .bind("orderId", payment.getOrderId())
                .fetch().one()
                .map(row -> Payment.createPayment()
                        .paymentId((Long) row.get("payment_id"))
                        .amount((BigDecimal) row.get("amount"))
                        .paymentStatus(PaymentStatus.valueOf((String) row.get("payment_status")))
                        .paymentMethod(PaymentMethod.valueOf((String) row.get("payment_method")))
                        .orderId((Long) row.get("order_id"))
                        .build());

    }


    public Mono<Payment> findById(Long paymentId) {
        String query = """
                select * 
                from payment p 
                where p.payment_id = :paymentId
                """;

        return databaseClient.sql(query)
                .bind("paymentId", paymentId)
                .fetch().one()
                .map(row -> Payment.createPayment()
                        .paymentId((Long) row.get("payment_id"))
                        .amount((BigDecimal) row.get("amount"))
                        .paymentStatus(PaymentStatus.valueOf((String) row.get("payment_status")))
                        .paymentMethod(PaymentMethod.valueOf((String) row.get("payment_method")))
                        .orderId((Long) row.get("order_id"))
                        .build());
    }


}
