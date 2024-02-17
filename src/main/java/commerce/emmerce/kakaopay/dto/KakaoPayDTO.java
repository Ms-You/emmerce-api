package commerce.emmerce.kakaopay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class KakaoPayDTO {

    @Getter
    @NoArgsConstructor
    public static class PayReq {
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


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResp {
        private String tid;	// 결제 고유 번호, 20자
        private String cid;	// 가맹점 코드
        private String status;	// 결제 상태
        private String partner_order_id;	// 가맹점 주문번호
        private String partner_user_id;	// 가맹점 회원 id
        private String payment_method_type;	// 결제 수단, CARD 또는 MONEY 중 하나
        private Amount amount;	// 결제 금액
        private CanceledAmount canceled_amount;	// 취소된 금액
        private CanceledAvailableAmount cancel_available_amount;	// 취소 가능 금액
        private String item_name;	// 상품 이름, 최대 100자
        private String item_code;	// 상품 코드, 최대 100자
        private Integer quantity;	// 상품 수량
        private String created_at;	// 결제 준비 요청 시각
        private String approved_at;	// 결제 승인 시각
        private String canceled_at;	// 결제 취소 시각
        private SelectedCardInfo selected_card_info;	// 결제 카드 정보
        private PaymentActionDetails[] payment_action_details;	// 결제/취소 상세
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundResp {
        private String aid;	// 요청 고유 번호
        private String tid;	// 결제 고유 번호, 10자
        private String cid;	// 가맹점 코드, 20자
        private String status;	// 결제 상태
        private String partner_order_id;	// 가맹점 주문번호, 최대 100자
        private String partner_user_id;	// 가맹점 회원 id, 최대 100자
        private String payment_method_type;	// 결제 수단, CARD 또는 MONEY 중 하나
        private Amount amount;	// 결제 금액 정보
        private ApprovedCancelAmount approved_cancel_amount;	// 이번 요청으로 취소된 금액
        private CanceledAmount canceled_amount;	// 누계 취소 금액
        private CancelAvailableAmount cancel_available_amount;	// 남은 취소 가능 금액
        private String item_name;	// 상품 이름, 최대 100자
        private String item_code;	// 상품 코드, 최대 100자
        private Integer quantity;	// 상품 수량
        private String created_at;	// 결제 준비 요청 시각
        private String approved_at;	// 결제 승인 시각
        private String canceled_at;	// 결제 취소 시각
        private String payload;	// 취소 요청 시 전달한 값
    }

}
