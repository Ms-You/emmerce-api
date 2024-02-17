package commerce.emmerce.domain;

import lombok.Getter;

@Getter
public enum PaymentStatus {

    COMPLETE("결제완료"), ING("결제중"), FAILED("결제실패"), REFUND("환불"), CANCEL("취소");

    private String value;

    PaymentStatus(String value) {
        this.value = value;
    }

}
