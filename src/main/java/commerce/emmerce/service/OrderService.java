package commerce.emmerce.service;

import commerce.emmerce.config.SecurityUtil;
import commerce.emmerce.domain.*;
import commerce.emmerce.dto.DeliveryDTO;
import commerce.emmerce.dto.OrderDTO;
import commerce.emmerce.dto.PaymentDTO;
import commerce.emmerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final MemberRepositoryImpl memberRepository;
    private final OrderRepositoryImpl orderRepository;
    private final OrderProductRepositoryImpl orderProductRepository;
    private final ProductRepositoryImpl productRepository;
    private final DeliveryRepositoryImpl deliveryRepository;
    private final PaymentRepositoryImpl paymentRepository;


    /**
     * 주문 시작
     * @param orderReq
     * @return
     */
    @Transactional
    public Mono<Void> startOrder(OrderDTO.OrderReq orderReq) {
        return SecurityUtil.getCurrentMemberName()
                .flatMap(name -> memberRepository.findByName(name)
                        .flatMap(member -> makeOrder(member, orderReq)
                        )
                );
    }

    /**
     * 주문 정보 생성
     * @param member
     * @param orderReq
     * @return
     */
    private Mono<Void> makeOrder(Member member, OrderDTO.OrderReq orderReq) {
        return orderRepository.save(Order.createOrder()
                        .orderDate(LocalDateTime.now())
                        .orderStatus(OrderStatus.COMPLETE)
                        .memberId(member.getMemberId())
                        .build())
                .doOnSuccess(savedOrder -> log.info("생성된 order_id: {}", savedOrder.getOrderId()))
                .flatMap(savedOrder -> saveProductsForOrder(savedOrder, orderReq.getOrderProductList())
                        .then(createDeliveryForOrder(savedOrder, orderReq.getDeliveryReq()))
                        .then(createPaymentForOrder(savedOrder, orderReq.getPaymentReq()))
                );
    }

    /**
     * 주문에 상품 정보 추가
     * @param order
     * @param orderProductReqList
     * @return
     */
    private Mono<Void> saveProductsForOrder(Order order, List<OrderDTO.OrderProductReq> orderProductReqList) {
        return Flux.fromIterable(orderProductReqList)
                .flatMap(orderProductReq -> orderProductRepository.save(OrderProduct.builder()
                        .totalPrice(orderProductReq.getTotalPrice())
                        .totalCount(orderProductReq.getTotalCount())
                        .orderId(order.getOrderId())
                        .productId(orderProductReq.getProductId())
                        .build()))
                .then();
    }

    /**
     * 배송 정보 추가
     * @param order
     * @param deliveryReq
     * @return
     */
    private Mono<Void> createDeliveryForOrder(Order order, DeliveryDTO.DeliveryReq deliveryReq) {
        return deliveryRepository.save(Delivery.createDelivery()
                .name(deliveryReq.getName())
                .tel(deliveryReq.getTel())
                .email(deliveryReq.getEmail())
                .city(deliveryReq.getCity())
                .street(deliveryReq.getStreet())
                .zipcode(deliveryReq.getZipcode())
                .deliveryStatus(DeliveryStatus.READY)
                .orderId(order.getOrderId())
                .build());
    }

    /**
     * 결제 정보 추가
     * @param order
     * @param paymentReq
     * @return
     */
    private Mono<Void> createPaymentForOrder(Order order, PaymentDTO.PaymentReq paymentReq) {
        return paymentRepository.save(Payment.createPayment()
                        .amount(paymentReq.getAmount())
                        .paymentStatus(paymentReq.getPaymentStatus())
                        .paymentMethod(paymentReq.getPaymentMethod())
                        .orderId(order.getOrderId())
                        .build())
                .then();
    }


    /**
     * 주문 목록 조회
     * @return
     */
    @Transactional
    public Flux<OrderDTO.OrderResp> getOrderList() {
        return SecurityUtil.getCurrentMemberName()
                .flatMap(name -> memberRepository.findByName(name))
                .flatMapMany(member -> findOrders(member));
    }

    /**
     * 로그인한 사용자의 주문 목록 조회
     * @param member
     * @return
     */
    public Flux<OrderDTO.OrderResp> findOrders(Member member) {
        return orderRepository.findByMemberId(member.getMemberId())
                .flatMap(order -> findOrderProducts(order)
                        .map(product -> OrderDTO.OrderProductResp.builder()
                                .productId(product.getProductId())
                                .name(product.getName())
                                .titleImgList(product.getTitleImgList())
                                .seller(product.getSeller())
                                .build()
                        ).collectList()
                        .map(orderProductResps -> OrderDTO.OrderResp.builder()
                                .orderId(order.getOrderId())
                                .orderDate(order.getOrderDate())
                                .orderStatus(order.getOrderStatus())
                                .orderProductRespList(orderProductResps)
                                .build())
                );
    }

    /**
     * 주문 상품 목록 조회
     * @param order
     * @return
     */
    public Flux<Product> findOrderProducts(Order order) {
        return orderProductRepository.findByOrderId(order.getOrderId())
                .flatMap(orderProduct -> findProducts(orderProduct));
    }

    /**
     * 상품 정보 조회
     * @param orderProduct
     * @return
     */
    public Mono<Product> findProducts(OrderProduct orderProduct) {
        return productRepository.findById(orderProduct.getProductId());
    }

}
