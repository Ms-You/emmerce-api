package commerce.emmerce.domain;

import lombok.Getter;

@Getter
public enum OrderStatus {

    ING("주문중"), COMPLETE("주문완료"), CANCEL("주문취소");

    private String value;

    OrderStatus(String value) {
        this.value = value;
    }
}
