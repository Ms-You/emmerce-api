package commerce.emmerce.kakaopay.service;

import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.domain.*;
import commerce.emmerce.kakaopay.dto.*;
import commerce.emmerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(KakaoPayService.class)
class KakaoPayServiceTest {
    @Autowired
    private KakaoPayService kakaoPayService;

    @MockBean
    private MemberRepository memberRepository;
    @MockBean
    private OrderRepository orderRepository;
    @MockBean
    private OrderProductRepository orderProductRepository;
    @MockBean
    private ProductRepository productRepository;
    @MockBean
    private PaymentRepository paymentRepository;
    @MockBean
    private DeliveryRepository deliveryRepository;
    @MockBean
    private WebClient webClient;

    private Member member;
    private Order order;
    private Product product1;
    private Product product2;
    private OrderProduct orderProduct1;
    private OrderProduct orderProduct2;
    private Delivery delivery1;
    private Delivery delivery2;
    private Payment payment;

    private SecurityContext securityContext;

    @BeforeEach
    void setup() {
        member = Member.createMember()
                .id(1L)
                .name("testId001")
                .email("test@test.com")
                .password("password")
                .tel("01012345678")
                .birth("240422")
                .point(0)
                .role(RoleType.ROLE_USER)
                .city("서울특별시")
                .street("공룡로 50")
                .zipcode("18888")
                .build();

        order = Order.createOrder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.ING)
                .memberId(member.getMemberId())
                .build();

        product1 = Product.createProduct()
                .productId(1L)
                .name("바지")
                .detail("찢어진 바지")
                .originalPrice(10000)
                .discountPrice(8000)
                .discountRate(20)
                .stockQuantity(100)
                .starScore(4.5)
                .totalReviews(20)
                .titleImg("바지 이미지")
                .detailImgList(List.of("바지 상세 이미지1", "바지 상세 이미지2"))
                .brand("이머스몰")
                .enrollTime(LocalDateTime.now())
                .build();

        product2 = Product.createProduct()
                .productId(2L)
                .name("티셔츠")
                .detail("T없이 맑은 셔츠")
                .originalPrice(16000)
                .discountPrice(8000)
                .discountRate(50)
                .stockQuantity(100)
                .starScore(4.8)
                .totalReviews(20)
                .titleImg("셔츠 이미지")
                .detailImgList(List.of("셔츠 상세 이미지1", "셔츠 상세 이미지2"))
                .brand("이머스몰")
                .enrollTime(LocalDateTime.now())
                .build();

        orderProduct1 = OrderProduct.builder()
                .orderProductId(1L)
                .totalPrice(40000)
                .totalCount(5)
                .orderId(order.getOrderId())
                .productId(product1.getProductId())
                .build();

        orderProduct2 = OrderProduct.builder()
                .orderProductId(2L)
                .totalPrice(32000)
                .totalCount(4)
                .orderId(order.getOrderId())
                .productId(product2.getProductId())
                .build();

        delivery1 = Delivery.createDelivery()
                .deliveryId(1L)
                .name("tester001")
                .tel("01012345678")
                .email("test@test.com")
                .city("서울특별시")
                .street("공룡로 50")
                .zipcode("18888")
                .deliveryStatus(DeliveryStatus.READY)
                .orderProductId(orderProduct1.getOrderProductId())
                .build();

        delivery2 = Delivery.createDelivery()
                .deliveryId(2L)
                .name("tester001")
                .tel("01012345678")
                .email("test@test.com")
                .city("서울특별시")
                .street("공룡로 50")
                .zipcode("18888")
                .deliveryStatus(DeliveryStatus.READY)
                .orderProductId(orderProduct2.getOrderProductId())
                .build();

        payment = Payment.builder()
                .aid("요청 고유 번호")
                .tid("결제 고유 번호")
                .cid("가맹점 코드")
                .partner_order_id("1")
                .partner_user_id("1")
                .payment_method_type("CARD")
                .total_amount(40000)
                .tax_free(0)
                .vat(4000)
                .point(0)
                .discount(10000)
                .green_deposit(0)
                .purchase_corp("매입 카드사 한글명")
                .purchase_corp_code("매입 카드사 코드")
                .issuer_corp("카드 발급사 한글명")
                .issuer_corp_code("카드 발급사 코드")
                .bin("카드 BIN")
                .card_type("카드 타입")
                .install_month("12")
                .approved_id("카드사 승인번호")
                .card_mid("카드사 가맹점 번호")
                .interest_free_install("Y")
                .card_item_code("카드 상품 코드")
                .item_name("샴푸")
                .quantity(5)
                .created_at(LocalDateTime.now())
                .approved_at(LocalDateTime.now())
                .paymentStatus(PaymentStatus.COMPLETE)
                .build();

        when(memberRepository.findByName(member.getName())).thenReturn(Mono.just(member));

        Authentication authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(member.getName());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(securityContext.getAuthentication().getName()).thenReturn(member.getName());
    }


    @Test
    @DisplayName("카카오페이 결제 준비 요청 테스트")
    void kakaoPayReady() {
        // given
        KakaoPayDTO.PayReq payReq = new KakaoPayDTO.PayReq(order.getOrderId());
        KakaoPayDTO.ReadyResp readyResp = new KakaoPayDTO.ReadyResp("결제 고유 번호", "사용자 정보 입력 화면", "요청 시간");

        // when
        when(orderRepository.findById(order.getOrderId())).thenReturn(Mono.just(order));
        when(orderProductRepository.findAllByOrderId(order.getOrderId())).thenReturn(Flux.just(orderProduct1, orderProduct2));
        when(productRepository.findById(orderProduct1.getProductId())).thenReturn(Mono.just(product1));
        when(productRepository.findById(orderProduct2.getProductId())).thenReturn(Mono.just(product2));
        when(paymentRepository.saveTemporary(any(Payment.class))).thenReturn(Mono.empty());

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(KakaoPayDTO.ReadyResp.class)).thenReturn(Mono.just(readyResp));

        StepVerifier.create(kakaoPayService.kakaoPayReady(payReq)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        ).expectNextMatches(result ->
                result.getTid().equals(readyResp.getTid()) &&
                        result.getNext_redirect_pc_url().equals(readyResp.getNext_redirect_pc_url()) &&
                        result.getCreated_at().equals(readyResp.getCreated_at())
        ).verifyComplete();

        // then
        verify(memberRepository, times(1)).findByName(anyString());
        verify(orderRepository, times(1)).findById(anyLong());
        verify(orderProductRepository, times(1)).findAllByOrderId(anyLong());
        verify(productRepository, times(1)).findById(anyLong());
        verify(paymentRepository, times(1)).saveTemporary(any(Payment.class));
    }

    @Test
    @DisplayName("카카오페이 결제 승인 요청 테스트")
    void kakaoPayApprove() {
        // given
        String pgToken = "pg_token";

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
        when(paymentRepository.findByOrderId(order.getOrderId())).thenReturn(Mono.just(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(Mono.just(payment));
        when(orderRepository.findById(order.getOrderId())).thenReturn(Mono.just(order));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderProductRepository.findAllByOrderId(order.getOrderId())).thenReturn(Flux.just(orderProduct1, orderProduct2));
        when(productRepository.findById(orderProduct1.getProductId())).thenReturn(Mono.just(product1));
        when(productRepository.findById(orderProduct2.getProductId())).thenReturn(Mono.just(product2));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.empty());

        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(KakaoPayDTO.ApproveResp.class)).thenReturn(Mono.just(approveResp));

        StepVerifier.create(kakaoPayService.kakaoPayApprove(pgToken, order.getOrderId())
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        ).expectNextMatches(result ->
                result.getAid().equals(approveResp.getAid()) &&
                        result.getTid().equals(approveResp.getTid()) &&
                        result.getCid().equals(approveResp.getCid()) &&
                        result.getSid().equals(approveResp.getSid()) &&
                        result.getPartner_order_id().equals(approveResp.getPartner_order_id()) &&
                        result.getPartner_user_id().equals(approveResp.getPartner_user_id()) &&
                        result.getPayment_method_type().equals(approveResp.getPayment_method_type()) &&
                        result.getAmount().getTotal() == approveResp.getAmount().getTotal() &&
                        result.getAmount().getTax_free() == approveResp.getAmount().getTax_free() &&
                        result.getAmount().getVat() == approveResp.getAmount().getVat() &&
                        result.getAmount().getPoint() == approveResp.getAmount().getPoint() &&
                        result.getAmount().getDiscount() == approveResp.getAmount().getDiscount() &&
                        result.getAmount().getGreen_deposit() == approveResp.getAmount().getGreen_deposit() &&
                        result.getCard_info().getPurchase_corp().equals(approveResp.getCard_info().getPurchase_corp()) &&
                        result.getCard_info().getPurchase_corp_code().equals(approveResp.getCard_info().getPurchase_corp_code()) &&
                        result.getCard_info().getIssuer_corp().equals(approveResp.getCard_info().getIssuer_corp()) &&
                        result.getCard_info().getIssuer_corp_code().equals(approveResp.getCard_info().getIssuer_corp_code()) &&
                        result.getCard_info().getBin().equals(approveResp.getCard_info().getBin()) &&
                        result.getCard_info().getCard_type().equals(approveResp.getCard_info().getCard_type()) &&
                        result.getCard_info().getInstall_month().equals(approveResp.getCard_info().getInstall_month()) &&
                        result.getCard_info().getApproved_id().equals(approveResp.getCard_info().getApproved_id()) &&
                        result.getCard_info().getCard_mid().equals(approveResp.getCard_info().getCard_mid()) &&
                        result.getCard_info().getInterest_free_install().equals(approveResp.getCard_info().getInterest_free_install()) &&
                        result.getCard_info().getCard_item_code().equals(approveResp.getCard_info().getCard_item_code()) &&
                        result.getItem_name().equals(approveResp.getItem_name()) &&
                        result.getItem_code().equals(approveResp.getItem_code()) &&
                        result.getQuantity() == approveResp.getQuantity() &&
                        result.getCreated_at().equals(approveResp.getCreated_at()) &&
                        result.getApproved_at().equals(approveResp.getApproved_at()) &&
                        result.getPayload().equals(approveResp.getPayload())
                ).verifyComplete();

        // then
        verify(paymentRepository, times(1)).findByOrderId(anyLong());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(orderRepository, times(1)).findById(anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProductRepository, times(1)).findAllByOrderId(anyLong());
        verify(productRepository, times(2)).findById(anyLong());
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    @DisplayName("카카오페이 결제 취소 요청 테스트 - 성공")
    void kakaoPayCancel_success() {
        // given
        KakaoPayDTO.PayReq payReq = new KakaoPayDTO.PayReq(order.getOrderId());

        // when
        when(orderRepository.findById(payReq.getOrderId())).thenReturn(Mono.just(order));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderProductRepository.findAllByOrderId(order.getOrderId())).thenReturn(Flux.just(orderProduct1, orderProduct2));
        when(deliveryRepository.findByOrderProductId(orderProduct1.getOrderProductId())).thenReturn(Mono.just(delivery1));
        when(deliveryRepository.findByOrderProductId(orderProduct2.getOrderProductId())).thenReturn(Mono.just(delivery2));
        when(deliveryRepository.updateStatus(delivery1.getDeliveryId(), orderProduct1.getOrderProductId(), DeliveryStatus.CANCEL)).thenReturn(Mono.empty());
        when(deliveryRepository.updateStatus(delivery2.getDeliveryId(), orderProduct2.getOrderProductId(), DeliveryStatus.CANCEL)).thenReturn(Mono.empty());
        when(paymentRepository.findByOrderId(order.getOrderId())).thenReturn(Mono.just(payment));
        when(paymentRepository.updateStatus(payment.getTid(), PaymentStatus.CANCEL)).thenReturn(Mono.empty());

        StepVerifier.create(kakaoPayService.kakaoPayCancel(payReq)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        ).verifyComplete();

        // then
        verify(orderRepository, times(2)).findById(anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProductRepository, times(1)).findAllByOrderId(anyLong());
        verify(deliveryRepository, times(2)).findByOrderProductId(anyLong());
        verify(deliveryRepository, times(2)).updateStatus(anyLong(), anyLong(), any(DeliveryStatus.class));
        verify(paymentRepository, times(1)).findByOrderId(anyLong());
        verify(paymentRepository, times(1)).updateStatus(anyString(), any(PaymentStatus.class));

    }

    @Test
    @DisplayName("카카오페이 결제 취소 요청 테스트 - 실패 (주문자 정보 불일치)")
    void kakaoPayCancel_failure_member_not_matched() {
        // given
        KakaoPayDTO.PayReq payReq = new KakaoPayDTO.PayReq(order.getOrderId());

        order = Order.createOrder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETE)
                .memberId(2L)
                .build();

        // when
        when(orderRepository.findById(payReq.getOrderId())).thenReturn(Mono.just(order));

        StepVerifier.create(kakaoPayService.kakaoPayCancel(payReq)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.ORDER_MEMBER_NOT_MATCHED)
                .verify();

        // then
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("카카오페이 결제 취소 요청 테스트 - 실패 (이미 취소된 주문)")
    void kakaoPayCancel_failure_order_already_canceled() {
        // given
        KakaoPayDTO.PayReq payReq = new KakaoPayDTO.PayReq(order.getOrderId());

        order = Order.createOrder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.CANCEL)
                .memberId(member.getMemberId())
                .build();

        // when
        when(orderRepository.findById(payReq.getOrderId())).thenReturn(Mono.just(order));

        StepVerifier.create(kakaoPayService.kakaoPayCancel(payReq)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.ORDER_ALREADY_CANCELED)
                .verify();

        // then
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("카카오페이 결제 정보 조회 테스트 - 성공")
    void kakaoPayInfo_success() {
        // given
        KakaoPayDTO.PayReq payReq = new KakaoPayDTO.PayReq(order.getOrderId());

        Amount amount = new Amount(40000, 0, 4000, 0, 10000, 0);
        CanceledAmount canceledAmount = new CanceledAmount(0, 0, 0, 0, 0, 0);
        CanceledAvailableAmount canceledAvailableAmount = new CanceledAvailableAmount(40000, 0, 4000, 0, 10000, 0);
        SelectedCardInfo selectedCardInfo = new SelectedCardInfo("11111", 0, "신한", "N");
        PaymentActionDetails[] paymentActionDetails = new PaymentActionDetails[1];
        paymentActionDetails[0] = new PaymentActionDetails("aid", LocalDateTime.now().toString(), 0, 0, 0, 0, "PAYMENT", "payload");

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
        when(orderRepository.findById(order.getOrderId())).thenReturn(Mono.just(order));
        when(paymentRepository.findByOrderId(order.getOrderId())).thenReturn(Mono.just(payment));

        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(KakaoPayDTO.OrderResp.class)).thenReturn(Mono.just(orderResp));

        StepVerifier.create(kakaoPayService.kakaoPayInfo(payReq)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        ).expectNextMatches(result ->
                result.getTid().equals(orderResp.getTid()) &&
                        result.getCid().equals(orderResp.getCid()) &&
                        result.getStatus().equals(orderResp.getStatus()) &&
                        result.getPartner_order_id().equals(orderResp.getPartner_order_id()) &&
                        result.getPartner_user_id().equals(orderResp.getPartner_user_id()) &&
                        result.getPayment_method_type().equals(orderResp.getPayment_method_type()) &&
                        result.getAmount().getTotal() == orderResp.getAmount().getTotal() &&
                        result.getAmount().getTax_free() == orderResp.getAmount().getTax_free() &&
                        result.getAmount().getVat() == orderResp.getAmount().getVat() &&
                        result.getAmount().getPoint() == orderResp.getAmount().getPoint() &&
                        result.getAmount().getDiscount() == orderResp.getAmount().getDiscount() &&
                        result.getAmount().getGreen_deposit() == orderResp.getAmount().getGreen_deposit() &&
                        result.getCanceled_amount().getTotal() == orderResp.getCanceled_amount().getTotal() &&
                        result.getCanceled_amount().getTax_free() == orderResp.getCanceled_amount().getTax_free() &&
                        result.getCanceled_amount().getVat() == orderResp.getCanceled_amount().getVat() &&
                        result.getCanceled_amount().getPoint() == orderResp.getCanceled_amount().getPoint() &&
                        result.getCanceled_amount().getDiscount() == orderResp.getCanceled_amount().getDiscount() &&
                        result.getCanceled_amount().getGreen_deposit() == orderResp.getCanceled_amount().getGreen_deposit() &&
                        result.getCancel_available_amount().getTotal() == orderResp.getCancel_available_amount().getTotal() &&
                        result.getCancel_available_amount().getTax_free() == orderResp.getCancel_available_amount().getTax_free() &&
                        result.getCancel_available_amount().getVat() == orderResp.getCancel_available_amount().getVat() &&
                        result.getCancel_available_amount().getPoint() == orderResp.getCancel_available_amount().getPoint() &&
                        result.getCancel_available_amount().getDiscount() == orderResp.getCancel_available_amount().getDiscount() &&
                        result.getCancel_available_amount().getGreen_deposit() == orderResp.getCancel_available_amount().getGreen_deposit() &&
                        result.getItem_name().equals(orderResp.getItem_name()) &&
                        result.getItem_code().equals(orderResp.getItem_code()) &&
                        result.getQuantity().equals(orderResp.getQuantity()) &&
                        result.getCreated_at().equals(orderResp.getCreated_at()) &&
                        result.getApproved_at().equals(orderResp.getApproved_at()) &&
                        result.getCanceled_at().equals(orderResp.getCanceled_at()) &&
                        result.getSelected_card_info().getCard_bin().equals(orderResp.getSelected_card_info().getCard_bin()) &&
                        result.getSelected_card_info().getInstall_month() == orderResp.getSelected_card_info().getInstall_month() &&
                        result.getSelected_card_info().getCard_corp_name().equals(orderResp.getSelected_card_info().getCard_corp_name()) &&
                        result.getSelected_card_info().getInterest_free_install().equals(orderResp.getSelected_card_info().getInterest_free_install()) &&
                        result.getPayment_action_details()[0].getAid().equals(orderResp.getPayment_action_details()[0].getAid()) &&
                        result.getPayment_action_details()[0].getApproved_at().equals(orderResp.getPayment_action_details()[0].getApproved_at()) &&
                        result.getPayment_action_details()[0].getAmount() == orderResp.getPayment_action_details()[0].getAmount() &&
                        result.getPayment_action_details()[0].getPoint_amount() == orderResp.getPayment_action_details()[0].getPoint_amount() &&
                        result.getPayment_action_details()[0].getDiscount_amount() == orderResp.getPayment_action_details()[0].getDiscount_amount() &&
                        result.getPayment_action_details()[0].getGreen_deposit() == orderResp.getPayment_action_details()[0].getGreen_deposit() &&
                        result.getPayment_action_details()[0].getPayment_action_type().equals(orderResp.getPayment_action_details()[0].getPayment_action_type()) &&
                        result.getPayment_action_details()[0].getPayload().equals(orderResp.getPayment_action_details()[0].getPayload())
                ).verifyComplete();

        // then
        verify(orderRepository, times(1)).findById(anyLong());
        verify(paymentRepository, times(1)).findByOrderId(anyLong());
    }

    @Test
    @DisplayName("카카오페이 결제 정보 조회 테스트 - 실패")
    void kakaoPayInfo_failure() {
        // given
        KakaoPayDTO.PayReq payReq = new KakaoPayDTO.PayReq(order.getOrderId());

        order = Order.createOrder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.ING)
                .memberId(2L)
                .build();

        // when
        when(orderRepository.findById(order.getOrderId())).thenReturn(Mono.just(order));

        StepVerifier.create(kakaoPayService.kakaoPayInfo(payReq)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.ORDER_MEMBER_NOT_MATCHED)
                .verify();

        // then
        verify(orderRepository, times(1)).findById(anyLong());
    }
}