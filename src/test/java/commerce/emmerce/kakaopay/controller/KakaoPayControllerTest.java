package commerce.emmerce.kakaopay.controller;

import commerce.emmerce.config.exception.GlobalErrorAttributes;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.kakaopay.dto.*;
import commerce.emmerce.kakaopay.service.KakaoPayService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser
@WebFluxTest(KakaoPayController.class)
class KakaoPayControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private KakaoPayService kakaoPayService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    @DisplayName("결제 준비 요청 테스트")
    void readyToPay() {
        // given
        KakaoPayDTO.PayReq payReq = new KakaoPayDTO.PayReq(1L);
        KakaoPayDTO.ReadyResp readyResp = new KakaoPayDTO.ReadyResp("결제 고유 번호", "사용자 정보 입력 화면", "요청 시간");

        // when
        when(kakaoPayService.kakaoPayReady(any(KakaoPayDTO.PayReq.class))).thenReturn(Mono.just(readyResp));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/payment/ready")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.tid").isEqualTo(readyResp.getTid())
                .jsonPath("$.next_redirect_pc_url").isEqualTo(readyResp.getNext_redirect_pc_url())
                .jsonPath("$.created_at").isEqualTo(readyResp.getCreated_at());

        // then
        verify(kakaoPayService).kakaoPayReady(any(KakaoPayDTO.PayReq.class));
    }

    @Test
    @DisplayName("결제 승인 요청 테스트")
    void success() {
        // given
        String pgToken = "pg_token";
        Long orderId = 1L;

        Amount amount = new Amount(40000, 0, 4000, 0, 10000, 0);
        CardInfo cardInfo = CardInfo.builder()
                .purchase_corp("이머스카드")
                .purchase_corp_code("05")
                .issuer_corp("이머스카드")
                .issuer_corp_code("05")
                .bin("111111")
                .card_type("신용")
                .install_month("00")
                .approved_id("11111111")
                .card_mid("111111")
                .interest_free_install("N")
                .card_item_code("111111")
                .build();

        KakaoPayDTO.ApproveResp approveResp = KakaoPayDTO.ApproveResp.builder()
                .aid("요청 고유 번호")
                .tid("결제 고유 번호")
                .cid("가맹점 코드")
                .sid("정기 결제용 ID")
                .partner_order_id("1")
                .partner_user_id("1")
                .payment_method_type("CARD")
                .amount(amount)
                .card_info(cardInfo)
                .item_name("샴푸")
                .item_code("상품 코드")
                .quantity(5)
                .created_at(LocalDateTime.now().toString())
                .approved_at(LocalDateTime.now().toString())
                .payload("결제 승인 요청 시 전달된 내용")
                .build();

        // when
        when(kakaoPayService.kakaoPayApprove(anyString(), anyLong())).thenReturn(Mono.just(approveResp));

        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/payment/success")
                        .queryParam("pg_token", pgToken)
                        .queryParam("orderId", orderId)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.aid").isEqualTo(approveResp.getAid())
                .jsonPath("$.tid").isEqualTo(approveResp.getTid())
                .jsonPath("$.cid").isEqualTo(approveResp.getCid())
                .jsonPath("$.sid").isEqualTo(approveResp.getSid())
                .jsonPath("$.partner_order_id").isEqualTo(approveResp.getPartner_order_id())
                .jsonPath("$.partner_user_id").isEqualTo(approveResp.getPartner_user_id())
                .jsonPath("$.payment_method_type").isEqualTo(approveResp.getPayment_method_type())
                .jsonPath("$.amount.total").isEqualTo(approveResp.getAmount().getTotal())
                .jsonPath("$.amount.tax_free").isEqualTo(approveResp.getAmount().getTax_free())
                .jsonPath("$.amount.vat").isEqualTo(approveResp.getAmount().getVat())
                .jsonPath("$.amount.point").isEqualTo(approveResp.getAmount().getPoint())
                .jsonPath("$.amount.discount").isEqualTo(approveResp.getAmount().getDiscount())
                .jsonPath("$.amount.green_deposit").isEqualTo(approveResp.getAmount().getGreen_deposit())
                .jsonPath("$.card_info.purchase_corp").isEqualTo(approveResp.getCard_info().getPurchase_corp())
                .jsonPath("$.card_info.purchase_corp_code").isEqualTo(approveResp.getCard_info().getPurchase_corp_code())
                .jsonPath("$.card_info.issuer_corp").isEqualTo(approveResp.getCard_info().getIssuer_corp())
                .jsonPath("$.card_info.issuer_corp_code").isEqualTo(approveResp.getCard_info().getIssuer_corp_code())
                .jsonPath("$.card_info.bin").isEqualTo(approveResp.getCard_info().getBin())
                .jsonPath("$.card_info.card_type").isEqualTo(approveResp.getCard_info().getCard_type())
                .jsonPath("$.card_info.install_month").isEqualTo(approveResp.getCard_info().getInstall_month())
                .jsonPath("$.card_info.approved_id").isEqualTo(approveResp.getCard_info().getApproved_id())
                .jsonPath("$.card_info.card_mid").isEqualTo(approveResp.getCard_info().getCard_mid())
                .jsonPath("$.card_info.interest_free_install").isEqualTo(approveResp.getCard_info().getInterest_free_install())
                .jsonPath("$.card_info.card_item_code").isEqualTo(approveResp.getCard_info().getCard_item_code())
                .jsonPath("$.item_name").isEqualTo(approveResp.getItem_name())
                .jsonPath("$.item_code").isEqualTo(approveResp.getItem_code())
                .jsonPath("$.quantity").isEqualTo(approveResp.getQuantity())
                .jsonPath("$.created_at").isEqualTo(approveResp.getCreated_at())
                .jsonPath("$.approved_at").isEqualTo(approveResp.getApproved_at())
                .jsonPath("$.payload").isEqualTo(approveResp.getPayload());

        // then
        verify(kakaoPayService).kakaoPayApprove(anyString(), anyLong());
    }

    @Test
    @DisplayName("결제 정보 조회 테스트")
    void lookUpOrder() {
        // given
        KakaoPayDTO.PayReq payReq = new KakaoPayDTO.PayReq(1L);

        Amount amount = new Amount(40000, 0, 4000, 0, 10000, 0);
        CanceledAmount canceledAmount = new CanceledAmount(0, 0, 0, 0, 0, 0);
        CanceledAvailableAmount canceledAvailableAmount = new CanceledAvailableAmount(40000, 0, 4000, 0, 10000, 0);
        SelectedCardInfo selectedCardInfo = new SelectedCardInfo("11111", 0, "신한", "N");
        PaymentActionDetails[] paymentActionDetails = new PaymentActionDetails[1];
        paymentActionDetails[0] = new PaymentActionDetails("aid", LocalDateTime.now().toString(), 0, 0, 0, 0, "PAYMENT", null);

        KakaoPayDTO.OrderResp orderResp = KakaoPayDTO.OrderResp.builder()
                .tid("결제 고유 번호")
                .cid("가맹점 코드")
                .status("SUCCESS_PAYMENT")
                .partner_order_id("1")
                .partner_user_id("1")
                .payment_method_type("CARD")
                .amount(amount)
                .canceled_amount(canceledAmount)
                .cancel_available_amount(canceledAvailableAmount)
                .item_name("샴푸")
                .item_code("상품 코드")
                .quantity(5)
                .created_at(LocalDateTime.now().toString())
                .approved_at(LocalDateTime.now().toString())
                .canceled_at(LocalDateTime.now().toString())
                .selected_card_info(selectedCardInfo)
                .payment_action_details(paymentActionDetails)
                .build();


        // when
        when(kakaoPayService.kakaoPayInfo(any(KakaoPayDTO.PayReq.class))).thenReturn(Mono.just(orderResp));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/payment/order")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.tid").isEqualTo(orderResp.getTid())
                .jsonPath("$.cid").isEqualTo(orderResp.getCid())
                .jsonPath("$.status").isEqualTo(orderResp.getStatus())
                .jsonPath("$.partner_order_id").isEqualTo(orderResp.getPartner_order_id())
                .jsonPath("$.partner_user_id").isEqualTo(orderResp.getPartner_user_id())
                .jsonPath("$.payment_method_type").isEqualTo(orderResp.getPayment_method_type())
                .jsonPath("$.amount.total").isEqualTo(orderResp.getAmount().getTotal())
                .jsonPath("$.amount.tax_free").isEqualTo(orderResp.getAmount().getTax_free())
                .jsonPath("$.amount.vat").isEqualTo(orderResp.getAmount().getVat())
                .jsonPath("$.amount.point").isEqualTo(orderResp.getAmount().getPoint())
                .jsonPath("$.amount.discount").isEqualTo(orderResp.getAmount().getDiscount())
                .jsonPath("$.amount.green_deposit").isEqualTo(orderResp.getAmount().getGreen_deposit())
                .jsonPath("$.canceled_amount.total").isEqualTo(orderResp.getCanceled_amount().getTotal())
                .jsonPath("$.canceled_amount.tax_free").isEqualTo(orderResp.getCanceled_amount().getTax_free())
                .jsonPath("$.canceled_amount.vat").isEqualTo(orderResp.getCanceled_amount().getVat())
                .jsonPath("$.canceled_amount.point").isEqualTo(orderResp.getCanceled_amount().getPoint())
                .jsonPath("$.canceled_amount.discount").isEqualTo(orderResp.getCanceled_amount().getDiscount())
                .jsonPath("$.canceled_amount.green_deposit").isEqualTo(orderResp.getCanceled_amount().getGreen_deposit())
                .jsonPath("$.cancel_available_amount.total").isEqualTo(orderResp.getCancel_available_amount().getTotal())
                .jsonPath("$.cancel_available_amount.tax_free").isEqualTo(orderResp.getCancel_available_amount().getTax_free())
                .jsonPath("$.cancel_available_amount.vat").isEqualTo(orderResp.getCancel_available_amount().getVat())
                .jsonPath("$.cancel_available_amount.point").isEqualTo(orderResp.getCancel_available_amount().getPoint())
                .jsonPath("$.cancel_available_amount.discount").isEqualTo(orderResp.getCancel_available_amount().getDiscount())
                .jsonPath("$.cancel_available_amount.green_deposit").isEqualTo(orderResp.getCancel_available_amount().getGreen_deposit())
                .jsonPath("$.item_name").isEqualTo(orderResp.getItem_name())
                .jsonPath("$.item_code").isEqualTo(orderResp.getItem_code())
                .jsonPath("$.quantity").isEqualTo(orderResp.getQuantity())
                .jsonPath("$.created_at").isEqualTo(orderResp.getCreated_at())
                .jsonPath("$.approved_at").isEqualTo(orderResp.getApproved_at())
                .jsonPath("$.canceled_at").isEqualTo(orderResp.getCanceled_at())
                .jsonPath("$.selected_card_info.card_bin").isEqualTo(orderResp.getSelected_card_info().getCard_bin())
                .jsonPath("$.selected_card_info.install_month").isEqualTo(orderResp.getSelected_card_info().getInstall_month())
                .jsonPath("$.selected_card_info.card_corp_name").isEqualTo(orderResp.getSelected_card_info().getCard_corp_name())
                .jsonPath("$.selected_card_info.interest_free_install").isEqualTo(orderResp.getSelected_card_info().getInterest_free_install())
                .jsonPath("$.payment_action_details[0].aid").isEqualTo(orderResp.getPayment_action_details()[0].getAid())
                .jsonPath("$.payment_action_details[0].approved_at").isEqualTo(orderResp.getPayment_action_details()[0].getApproved_at())
                .jsonPath("$.payment_action_details[0].amount").isEqualTo(orderResp.getPayment_action_details()[0].getAmount())
                .jsonPath("$.payment_action_details[0].point_amount").isEqualTo(orderResp.getPayment_action_details()[0].getPoint_amount())
                .jsonPath("$.payment_action_details[0].discount_amount").isEqualTo(orderResp.getPayment_action_details()[0].getDiscount_amount())
                .jsonPath("$.payment_action_details[0].green_deposit").isEqualTo(orderResp.getPayment_action_details()[0].getGreen_deposit())
                .jsonPath("$.payment_action_details[0].payment_action_type").isEqualTo(orderResp.getPayment_action_details()[0].getPayment_action_type())
                .jsonPath("$.payment_action_details[0].payload").isEqualTo(orderResp.getPayment_action_details()[0].getPayload());

        // then
        verify(kakaoPayService).kakaoPayInfo(any(KakaoPayDTO.PayReq.class));
    }

    @Test
    @DisplayName("결제 취소 테스트")
    void cancel() {
        // given
        KakaoPayDTO.PayReq payReq = new KakaoPayDTO.PayReq(1L);

        // when
        when(kakaoPayService.kakaoPayCancel(payReq)).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/payment/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(kakaoPayService).kakaoPayCancel(any(KakaoPayDTO.PayReq.class));
    }
}