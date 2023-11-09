package commerce.emmerce.kakaopay.dto;

import lombok.Data;

@Data
public class Amount {
    private int total;  // 전체 결제 금액
    private int tax_free;   // 비과세 금액
    private int vat;    // 부과세 금액
    private int point;  // 사용한 포인트 금액
    private int discount;   // 할인 금액
    private int green_deposit;  // 컵 보증금
}
