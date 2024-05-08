package commerce.emmerce.service;

import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.domain.*;
import commerce.emmerce.dto.DeliveryDTO;
import commerce.emmerce.dto.OrderDTO;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(OrderService.class)
class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderProductRepository orderProductRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private DeliveryRepository deliveryRepository;

    @MockBean
    private ReviewRepository reviewRepository;

    private Member member;
    private Order order;
    private Product product1;
    private Product product2;
    private OrderProduct orderProduct1;
    private OrderProduct orderProduct2;
    private Delivery delivery1;
    private Delivery delivery2;

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

        when(memberRepository.findByName(member.getName())).thenReturn(Mono.just(member));

        Authentication authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(member.getName());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(securityContext.getAuthentication().getName()).thenReturn(member.getName());
    }


    @Test
    @DisplayName("주문 생성 테스트 - 성공")
    void startOrder_success() {
        // given
        OrderDTO.OrderProductReq orderProductReq1 = new OrderDTO.OrderProductReq(product1.getProductId(), orderProduct1.getTotalCount());
        OrderDTO.OrderProductReq orderProductReq2 = new OrderDTO.OrderProductReq(product2.getProductId(), orderProduct2.getTotalCount());
        DeliveryDTO.DeliveryReq deliveryReq = DeliveryDTO.DeliveryReq.builder()
                .name(member.getName())
                .tel(member.getTel())
                .email(member.getEmail())
                .city("서울특별시")
                .street("공룡로 50")
                .zipcode("18888")
                .build();

        OrderDTO.OrderReq orderReq = new OrderDTO.OrderReq(List.of(orderProductReq1, orderProductReq2), deliveryReq);

        OrderDTO.OrderCreateResp orderCreateResp = new OrderDTO.OrderCreateResp(order.getOrderId());

        // when
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderProductRepository.findAllByOrderId(order.getOrderId())).thenReturn(Flux.just(orderProduct1, orderProduct2));
        when(productRepository.findById(product1.getProductId())).thenReturn(Mono.just(product1));
        when(productRepository.findById(product2.getProductId())).thenReturn(Mono.just(product2));
        when(orderProductRepository.save(any(OrderProduct.class))).thenReturn(Mono.empty());
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(Mono.empty());

        StepVerifier.create(orderService.startOrder(orderReq)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectNextMatches(result ->
                        result.getOrderId() == orderCreateResp.getOrderId())
                .verifyComplete();

        // then
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProductRepository, times(1)).findAllByOrderId(anyLong());
        verify(productRepository, times(2)).findById(anyLong());
        verify(orderProductRepository, times(2)).save(any(OrderProduct.class));
        verify(deliveryRepository, times(2)).save(any(Delivery.class));
    }

    @Test
    @DisplayName("주문 생성 테스트 - 실패")
    void startOrder_failure() {
        // given
        OrderDTO.OrderProductReq orderProductReq1 = new OrderDTO.OrderProductReq(product1.getProductId(), 101);
        OrderDTO.OrderProductReq orderProductReq2 = new OrderDTO.OrderProductReq(product2.getProductId(), 101);
        DeliveryDTO.DeliveryReq deliveryReq = DeliveryDTO.DeliveryReq.builder()
                .name(member.getName())
                .tel(member.getTel())
                .email(member.getEmail())
                .city("서울특별시")
                .street("공룡로 50")
                .zipcode("18888")
                .build();

        OrderDTO.OrderReq orderReq = new OrderDTO.OrderReq(List.of(orderProductReq1, orderProductReq2), deliveryReq);

        // when
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderProductRepository.findAllByOrderId(order.getOrderId())).thenReturn(Flux.just(orderProduct1, orderProduct2));
        when(productRepository.findById(product1.getProductId())).thenReturn(Mono.just(product1));
        when(productRepository.findById(product2.getProductId())).thenReturn(Mono.just(product2));

        StepVerifier.create(orderService.startOrder(orderReq)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("상품의 재고가 부족합니다"))
                .verify();

        // then
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 정보 조회 테스트 - 성공")
    void getOrderInfo_success() {
        // given
        order = Order.createOrder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETE)
                .memberId(member.getMemberId())
                .build();

        OrderDTO.OrderProductResp orderProductResp1 = OrderDTO.OrderProductResp.transfer(product1, orderProduct1, false, DeliveryStatus.READY);
        OrderDTO.OrderProductResp orderProductResp2 = OrderDTO.OrderProductResp.transfer(product2, orderProduct2, false, DeliveryStatus.READY);

        OrderDTO.OrderResp orderResp = OrderDTO.OrderResp.transfer(order, List.of(orderProductResp1, orderProductResp2));

        // when
        when(orderRepository.findById(order.getOrderId())).thenReturn(Mono.just(order));
        when(orderProductRepository.findAllByOrderId(order.getOrderId())).thenReturn(Flux.just(orderProduct1, orderProduct2));
        when(productRepository.findById(orderProduct1.getProductId())).thenReturn(Mono.just(product1));
        when(productRepository.findById(orderProduct2.getProductId())).thenReturn(Mono.just(product2));
        when(deliveryRepository.findByOrderProductId(orderProduct1.getOrderProductId())).thenReturn(Mono.just(delivery1));
        when(deliveryRepository.findByOrderProductId(orderProduct2.getOrderProductId())).thenReturn(Mono.just(delivery2));
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), orderProduct1.getOrderProductId())).thenReturn(Mono.just(0L));
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), orderProduct2.getOrderProductId())).thenReturn(Mono.just(0L));

        StepVerifier.create(orderService.getOrderInfo(order.getOrderId())
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectNextMatches(result ->
                        result.getOrderId() == orderResp.getOrderId() &&
                                result.getOrderStatus().equals(orderResp.getOrderStatus()) &&
                                result.getOrderProductRespList().get(0).getOrderProductId() == orderResp.getOrderProductRespList().get(0).getOrderProductId() &&
                                result.getOrderProductRespList().get(0).getProductId() == orderResp.getOrderProductRespList().get(0).getProductId() &&
                                result.getOrderProductRespList().get(0).getName().equals(orderResp.getOrderProductRespList().get(0).getName()) &&
                                result.getOrderProductRespList().get(0).getTitleImg().equals(orderResp.getOrderProductRespList().get(0).getTitleImg()) &&
                                result.getOrderProductRespList().get(0).getBrand().equals(orderResp.getOrderProductRespList().get(0).getBrand()) &&
                                result.getOrderProductRespList().get(0).getOriginalPrice() == orderResp.getOrderProductRespList().get(0).getOriginalPrice() &&
                                result.getOrderProductRespList().get(0).getDiscountPrice() == orderResp.getOrderProductRespList().get(0).getDiscountPrice() &&
                                result.getOrderProductRespList().get(0).getQuantity() == orderResp.getOrderProductRespList().get(0).getQuantity() &&
                                result.getOrderProductRespList().get(0).isReviewStatus() == orderResp.getOrderProductRespList().get(0).isReviewStatus() &&
                                result.getOrderProductRespList().get(0).getDeliveryStatus().equals(orderResp.getOrderProductRespList().get(0).getDeliveryStatus()) &&
                                result.getOrderProductRespList().get(1).getOrderProductId() == orderResp.getOrderProductRespList().get(1).getOrderProductId() &&
                                result.getOrderProductRespList().get(1).getProductId() == orderResp.getOrderProductRespList().get(1).getProductId() &&
                                result.getOrderProductRespList().get(1).getName().equals(orderResp.getOrderProductRespList().get(1).getName()) &&
                                result.getOrderProductRespList().get(1).getTitleImg().equals(orderResp.getOrderProductRespList().get(1).getTitleImg()) &&
                                result.getOrderProductRespList().get(1).getBrand().equals(orderResp.getOrderProductRespList().get(1).getBrand()) &&
                                result.getOrderProductRespList().get(1).getOriginalPrice() == orderResp.getOrderProductRespList().get(1).getOriginalPrice() &&
                                result.getOrderProductRespList().get(1).getDiscountPrice() == orderResp.getOrderProductRespList().get(1).getDiscountPrice() &&
                                result.getOrderProductRespList().get(1).getQuantity() == orderResp.getOrderProductRespList().get(1).getQuantity() &&
                                result.getOrderProductRespList().get(1).isReviewStatus() == orderResp.getOrderProductRespList().get(1).isReviewStatus() &&
                                result.getOrderProductRespList().get(1).getDeliveryStatus().equals(orderResp.getOrderProductRespList().get(1).getDeliveryStatus()))
                .verifyComplete();

        // then
        verify(orderRepository, times(1)).findById(anyLong());
        verify(orderProductRepository, times(1)).findAllByOrderId(anyLong());
        verify(productRepository, times(2)).findById(anyLong());
        verify(deliveryRepository, times(2)).findByOrderProductId(anyLong());
        verify(reviewRepository, times(2)).findByMemberAndOrderProduct(anyLong(), anyLong());
    }

    @Test
    @DisplayName("주문 정보 조회 테스트 - 실패 (잘못된 주문 정보)")
    void getOrderInfo_failure_wrong_orderId() {
        // given
        // when
        when(orderRepository.findById(2L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getOrderInfo(2L)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.ORDER_NOT_FOUND)
                .verify();

        // then
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("주문 정보 조회 테스트 - 실패 (현재 사용자와 주문자 정보 불일치)")
    void getOrderInfo_failure_member_not_matched() {
        // given
        order = Order.createOrder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETE)
                .memberId(2L)
                .build();

        // when
        when(orderRepository.findById(order.getOrderId())).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.getOrderInfo(order.getOrderId())
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.ORDER_MEMBER_NOT_MATCHED)
                .verify();

        // then
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("주문 정보 조회 테스트 - 실패 (주문이 완료되지 않음)")
    void getOrderInfo_failure_order_status_not_complete() {
        // given
        // when
        when(orderRepository.findById(order.getOrderId())).thenReturn(Mono.just(order));

        StepVerifier.create(orderService.getOrderInfo(order.getOrderId())
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.ORDER_NOT_COMPLETED)
                .verify();

        // then
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("주문 정보 조회 테스트 - 실패 (배송 정보를 찾을 수 없음)")
    void getOrderInfo_failure_delivery_not_found() {
        // given
        order = Order.createOrder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETE)
                .memberId(member.getMemberId())
                .build();

        // when
        when(orderRepository.findById(order.getOrderId())).thenReturn(Mono.just(order));
        when(orderProductRepository.findAllByOrderId(order.getOrderId())).thenReturn(Flux.just(orderProduct1, orderProduct2));
        when(productRepository.findById(orderProduct1.getProductId())).thenReturn(Mono.just(product1));
        when(productRepository.findById(orderProduct2.getProductId())).thenReturn(Mono.just(product2));
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), orderProduct1.getOrderProductId())).thenReturn(Mono.just(0L));
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), orderProduct2.getOrderProductId())).thenReturn(Mono.just(0L));
        when(deliveryRepository.findByOrderProductId(orderProduct1.getOrderProductId())).thenReturn(Mono.empty());
        when(deliveryRepository.findByOrderProductId(orderProduct2.getOrderProductId())).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getOrderInfo(order.getOrderId())
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.DELIVERY_NOT_FOUND_BY_ORDER_PRODUCT)
                .verify();

        // then
        verify(orderRepository, times(1)).findById(anyLong());
        verify(orderProductRepository, times(1)).findAllByOrderId(anyLong());
        verify(deliveryRepository, times(1)).findByOrderProductId(anyLong());
        verify(reviewRepository, times(1)).findByMemberAndOrderProduct(anyLong(), anyLong());
    }

    @Test
    @DisplayName("현재 사용자의 주문 목록 조회 테스트 - 성공")
    void getOrderList_success() {
        // given
        order = Order.createOrder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETE)
                .memberId(member.getMemberId())
                .build();

        OrderDTO.OrderProductResp orderProductResp1 = OrderDTO.OrderProductResp.transfer(product1, orderProduct1, false, DeliveryStatus.READY);
        OrderDTO.OrderProductResp orderProductResp2 = OrderDTO.OrderProductResp.transfer(product2, orderProduct2, false, DeliveryStatus.READY);

        OrderDTO.OrderResp orderResp = OrderDTO.OrderResp.transfer(order, List.of(orderProductResp1, orderProductResp2));

        // when
        when(orderRepository.findByMemberId(order.getMemberId())).thenReturn(Flux.just(order));
        when(orderProductRepository.findAllByOrderId(order.getOrderId())).thenReturn(Flux.just(orderProduct1, orderProduct2));
        when(productRepository.findById(orderProduct1.getProductId())).thenReturn(Mono.just(product1));
        when(productRepository.findById(orderProduct2.getProductId())).thenReturn(Mono.just(product2));
        when(deliveryRepository.findByOrderProductId(orderProduct1.getOrderProductId())).thenReturn(Mono.just(delivery1));
        when(deliveryRepository.findByOrderProductId(orderProduct2.getOrderProductId())).thenReturn(Mono.just(delivery2));
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), orderProduct1.getOrderProductId())).thenReturn(Mono.just(0L));
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), orderProduct2.getOrderProductId())).thenReturn(Mono.just(0L));

        StepVerifier.create(orderService.findOrders(member)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                )
                .expectNextMatches(result ->
                        result.getOrderId() == orderResp.getOrderId() &&
                                result.getOrderStatus().equals(orderResp.getOrderStatus()) &&
                                result.getOrderProductRespList().get(0).getOrderProductId() == orderResp.getOrderProductRespList().get(0).getOrderProductId() &&
                                result.getOrderProductRespList().get(0).getProductId() == orderResp.getOrderProductRespList().get(0).getProductId() &&
                                result.getOrderProductRespList().get(0).getName().equals(orderResp.getOrderProductRespList().get(0).getName()) &&
                                result.getOrderProductRespList().get(0).getTitleImg().equals(orderResp.getOrderProductRespList().get(0).getTitleImg()) &&
                                result.getOrderProductRespList().get(0).getBrand().equals(orderResp.getOrderProductRespList().get(0).getBrand()) &&
                                result.getOrderProductRespList().get(0).getOriginalPrice() == orderResp.getOrderProductRespList().get(0).getOriginalPrice() &&
                                result.getOrderProductRespList().get(0).getDiscountPrice() == orderResp.getOrderProductRespList().get(0).getDiscountPrice() &&
                                result.getOrderProductRespList().get(0).getQuantity() == orderResp.getOrderProductRespList().get(0).getQuantity() &&
                                result.getOrderProductRespList().get(0).isReviewStatus() == orderResp.getOrderProductRespList().get(0).isReviewStatus() &&
                                result.getOrderProductRespList().get(0).getDeliveryStatus().equals(orderResp.getOrderProductRespList().get(0).getDeliveryStatus()) &&
                                result.getOrderProductRespList().get(1).getOrderProductId() == orderResp.getOrderProductRespList().get(1).getOrderProductId() &&
                                result.getOrderProductRespList().get(1).getProductId() == orderResp.getOrderProductRespList().get(1).getProductId() &&
                                result.getOrderProductRespList().get(1).getName().equals(orderResp.getOrderProductRespList().get(1).getName()) &&
                                result.getOrderProductRespList().get(1).getTitleImg().equals(orderResp.getOrderProductRespList().get(1).getTitleImg()) &&
                                result.getOrderProductRespList().get(1).getBrand().equals(orderResp.getOrderProductRespList().get(1).getBrand()) &&
                                result.getOrderProductRespList().get(1).getOriginalPrice() == orderResp.getOrderProductRespList().get(1).getOriginalPrice() &&
                                result.getOrderProductRespList().get(1).getDiscountPrice() == orderResp.getOrderProductRespList().get(1).getDiscountPrice() &&
                                result.getOrderProductRespList().get(1).getQuantity() == orderResp.getOrderProductRespList().get(1).getQuantity() &&
                                result.getOrderProductRespList().get(1).isReviewStatus() == orderResp.getOrderProductRespList().get(1).isReviewStatus() &&
                                result.getOrderProductRespList().get(1).getDeliveryStatus().equals(orderResp.getOrderProductRespList().get(1).getDeliveryStatus()))
                .verifyComplete();

        // then
        verify(orderRepository, times(1)).findByMemberId(anyLong());
        verify(orderProductRepository, times(1)).findAllByOrderId(anyLong());
        verify(productRepository, times(2)).findById(anyLong());
        verify(deliveryRepository, times(2)).findByOrderProductId(anyLong());
        verify(reviewRepository, times(2)).findByMemberAndOrderProduct(anyLong(), anyLong());
    }

    @Test
    @DisplayName("현재 사용자의 주문 목록 조회 테스트 - 성공 (주문 상태 필터)")
    void getOrderList_success_filter() {
        // given
        // when
        when(orderRepository.findByMemberId(order.getMemberId())).thenReturn(Flux.just(order));

        StepVerifier.create(orderService.findOrders(member)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        ).verifyComplete();

        // then
        verify(orderRepository, times(1)).findByMemberId(anyLong());
    }

    @Test
    @DisplayName("현재 사용자의 주문 목록 조회 테스트 - 실패 (배송 정보를 찾을 수 없음)")
    void getOrderList_failure_delivery_not_found() {
        // given
        order = Order.createOrder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETE)
                .memberId(member.getMemberId())
                .build();

        // when
        when(orderRepository.findByMemberId(order.getMemberId())).thenReturn(Flux.just(order));
        when(orderProductRepository.findAllByOrderId(order.getOrderId())).thenReturn(Flux.just(orderProduct1, orderProduct2));
        when(productRepository.findById(orderProduct1.getProductId())).thenReturn(Mono.just(product1));
        when(productRepository.findById(orderProduct2.getProductId())).thenReturn(Mono.just(product2));
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), orderProduct1.getOrderProductId())).thenReturn(Mono.just(0L));
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), orderProduct2.getOrderProductId())).thenReturn(Mono.just(0L));
        when(deliveryRepository.findByOrderProductId(orderProduct1.getOrderProductId())).thenReturn(Mono.empty());
        when(deliveryRepository.findByOrderProductId(orderProduct2.getOrderProductId())).thenReturn(Mono.empty());

        StepVerifier.create(orderService.findOrders(member)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.DELIVERY_NOT_FOUND_BY_ORDER_PRODUCT)
                .verify();

        // then
        verify(orderRepository, times(1)).findByMemberId(anyLong());
        verify(orderProductRepository, times(1)).findAllByOrderId(anyLong());
        verify(productRepository, times(1)).findById(anyLong());
        verify(deliveryRepository, times(1)).findByOrderProductId(anyLong());
        verify(reviewRepository, times(1)).findByMemberAndOrderProduct(anyLong(), anyLong());
    }

    @Test
    @DisplayName("주문 상품 목록 조회 테스트")
    void findOrderProducts() {
        // given
        // when
        when(orderProductRepository.findAllByOrderId(order.getOrderId())).thenReturn(Flux.just(orderProduct1, orderProduct2));

        StepVerifier.create(orderService.findOrderProducts(order))
                .expectNext(orderProduct1)
                .expectNext(orderProduct2)
                .verifyComplete();

        // then
        verify(orderProductRepository, times(1)).findAllByOrderId(anyLong());
    }

    @Test
    @DisplayName("상품 정보 조회 테스트")
    void findProducts() {
        // given
        // when
        when(productRepository.findById(product1.getProductId())).thenReturn(Mono.just(product1));

        StepVerifier.create(orderService.findProducts(orderProduct1))
                .expectNextMatches(result ->
                        result.getProductId() == product1.getProductId() &&
                                result.getName().equals(product1.getName()) &&
                                result.getDetail().equals(product1.getDetail()) &&
                                result.getOriginalPrice() == product1.getOriginalPrice() &&
                                result.getDiscountPrice() == product1.getDiscountPrice() &&
                                result.getDiscountRate() == product1.getDiscountRate() &&
                                result.getStarScore() == product1.getStarScore() &&
                                result.getTotalReviews() == product1.getTotalReviews() &&
                                result.getTitleImg().equals(product1.getTitleImg()) &&
                                result.getDetailImgList().size() == product1.getDetailImgList().size() &&
                                result.getBrand().equals(product1.getBrand())
                ).verifyComplete();

        // then
        verify(productRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("리뷰 작성 여부 조회 테스트 - 없을 때")
    void checkReviewWrote_no_review() {
        // given
        // when
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), orderProduct1.getOrderProductId())).thenReturn(Mono.just(0L));

        StepVerifier.create(orderService.checkReviewWrote(member, orderProduct1.getOrderProductId()))
                .expectNextMatches(result -> result == false)
                .verifyComplete();

        // then
        verify(reviewRepository, times(1)).findByMemberAndOrderProduct(anyLong(), anyLong());
    }

    @Test
    @DisplayName("리뷰 작성 여부 조회 테스트 - 있을 때")
    void checkReviewWrote_review() {
        // given
        // when
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), orderProduct1.getOrderProductId())).thenReturn(Mono.just(1L));

        StepVerifier.create(orderService.checkReviewWrote(member, orderProduct1.getOrderProductId()))
                .expectNextMatches(result -> result == true)
                .verifyComplete();

        // then
        verify(reviewRepository, times(1)).findByMemberAndOrderProduct(anyLong(), anyLong());
    }
}