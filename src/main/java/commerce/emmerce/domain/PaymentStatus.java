package commerce.emmerce.domain;

public enum PaymentStatus {

    PAID("결제완료"), PENDING("결제중"), FAILED("결제실패");

    private String value;

    PaymentStatus(String value) {
        this.value = value;
    }

}
