package commerce.emmerce.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("payment")
public class Payment {

    @Id
    @Column(value = "payment_id")
    private Long paymentId;

    private Integer amount;

    private PaymentStatus paymentStatus;

    private PaymentMethod paymentMethod;

    private Long orderId;


    @Builder(builderMethodName = "createPayment")
    public Payment(Long paymentId, Integer amount, PaymentStatus paymentStatus, PaymentMethod paymentMethod, Long orderId) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
    }


    public void updateStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
