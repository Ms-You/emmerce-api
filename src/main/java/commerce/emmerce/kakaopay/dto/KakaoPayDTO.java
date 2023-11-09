package commerce.emmerce.kakaopay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class KakaoPayDTO {

    @Getter
    @NoArgsConstructor
    public static class ReadyReq {
        private Long orderId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadyResp {
        private String tid; // 결제 고유 번호
        private String next_redirect_pc_url;    // pc 웹일 경우 사용자 정보 입력 화면
        private String created_at;  // 결제 준비 요청 시간
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproveResp {
        private String aid; // 요청 고유 번호
        private String tid; // 결제 고유 번호
        private String cid; // 가맹점 코드
        private String sid; // 정기 결제용 ID
        private String partner_order_id;    // 가맹점 주문 번호
        private String partner_user_id; // 가맹점 회원 id
        private String payment_method_type; // 결제 수단 (CARD 또는 MONEY)
        private Amount amount;  // 결제 금액 정보
        private CardInfo card_info; // 결제 상세 정보 (결제 수단이 카드일 경우만 포함)
        private String item_name;   // 상품 이름
        private String item_code;   // 상품 코드
        private Integer quantity;    // 상품 수량
        private String created_at;  // 결제 준비 요청 시각
        private String approved_at; // 결제 승인 시각
        private String payload; // 결제 승인 요청 시 전달된 내용
    }

}
