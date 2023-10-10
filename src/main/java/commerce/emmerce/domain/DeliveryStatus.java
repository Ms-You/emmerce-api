package commerce.emmerce.domain;

import lombok.Getter;

@Getter
public enum DeliveryStatus {

    READY("준비중"), ING("배송중"), COMPLETE("배송완료"), CANCEL("배송취소");

    private String value;

    DeliveryStatus(String value) {
        this.value = value;
    }
}
