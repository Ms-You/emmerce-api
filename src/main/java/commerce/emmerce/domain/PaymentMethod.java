package commerce.emmerce.domain;

public enum PaymentMethod {

    CREDIT_CARD("신용카드"), ACCOUNT_TRANSFER("계좌이체");

    private String value;

    PaymentMethod(String value) {
        this.value = value;
    }
}
