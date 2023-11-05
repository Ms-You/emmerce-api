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
    private final OrderRepositoryImpl customOrderRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepositoryImpl orderProductRepository;
    private final ProductRepositoryImpl customProductRepository;
    private final ProductRepository productRepository;
    private final DeliveryRepositoryImpl customDeliveryRepository;
    private final DeliveryRepository deliveryRepository;
    private final PaymentRepositoryImpl customPaymentRepository;
    private final PaymentRepository paymentRepository;


    //== 주문 생성 로직 시작 ==//

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
        return customOrderRepository.save(Order.createOrder()
                        .orderDate(LocalDateTime.now())
                        .orderStatus(OrderStatus.COMPLETE)
                        .memberId(member.getMemberId())
                        .build())
                .doOnSuccess(savedOrder -> log.info("생성된 order_id: {}", savedOrder.getOrderId()))
                .flatMap(savedOrder -> saveProductsForOrder(savedOrder, orderReq.getOrderProductList())
                        .flatMap(totalPrice -> createDeliveryForOrder(savedOrder, orderReq.getDeliveryReq())
                            .then(createPaymentForOrder(savedOrder, orderReq.getPaymentReq(), totalPrice))
                        )
                );
    }


    /**
     * 주문에 상품 정보 추가
     * @param order
     * @param orderProductReqList
     * @return
     */
    private Mono<Integer> saveProductsForOrder(Order order, List<OrderDTO.OrderProductReq> orderProductReqList) {
        return Flux.fromIterable(orderProductReqList)
                .flatMap(orderProductReq -> customProductRepository.findById(orderProductReq.getProductId())
                        .flatMap(product -> {
                            if(product.getStockQuantity() < orderProductReq.getTotalCount()) {
                                return Mono.error(new RuntimeException("'" + product.getName() + "' 상품의 재고가 부족합니다."));
                            } else {
                                // 재고 업데이트
                                int stockQuantity = product.getStockQuantity() - orderProductReq.getTotalCount();
                                product.updateStockQuantity(stockQuantity);

                                // 각 상품의 구매 금액
                                int eachTotalPrice = product.getDiscountPrice() * orderProductReq.getTotalCount();

                                return orderProductRepository.save(OrderProduct.builder()
                                            .totalPrice(eachTotalPrice)
                                            .totalCount(orderProductReq.getTotalCount())
                                            .orderId(order.getOrderId())
                                            .productId(orderProductReq.getProductId())
                                            .build())
                                        .then(productRepository.save(product))
                                        .thenReturn(eachTotalPrice);
                            }
                        })
                ).reduce(Integer::sum);
    }

    /**
     * 배송 정보 추가
     * @param order
     * @param deliveryReq
     * @return
     */
    private Mono<Void> createDeliveryForOrder(Order order, DeliveryDTO.DeliveryReq deliveryReq) {
        return customDeliveryRepository.save(Delivery.createDelivery()
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
    private Mono<Void> createPaymentForOrder(Order order, PaymentDTO.PaymentReq paymentReq, int totalPrice) {
        return customPaymentRepository.save(Payment.createPayment()
                        .amount(totalPrice)
                        .paymentStatus(PaymentStatus.PAID)
                        .paymentMethod(paymentReq.getPaymentMethod())
                        .orderId(order.getOrderId())
                        .build())
                .then();
    }


    //== 주문 생성 로직 끝 ==//

    //== 주문 조회 로직 시작 ==//

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
        return customOrderRepository.findByMemberId(member.getMemberId())
                .flatMap(order -> findOrderProducts(order)
                        .map(product -> OrderDTO.OrderProductResp.builder()
                                .productId(product.getProductId())
                                .name(product.getName())
                                .titleImg(product.getTitleImg())
                                .brand(product.getBrand())
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
        return orderProductRepository.findAllByOrderId(order.getOrderId())
                .flatMap(orderProduct -> findProducts(orderProduct));
    }

    /**
     * 상품 정보 조회
     * @param orderProduct
     * @return
     */
    public Mono<Product> findProducts(OrderProduct orderProduct) {
        return customProductRepository.findById(orderProduct.getProductId());
    }


    //== 주문 조회 로직 끝 ==//

    //== 주문 취소 로직 시작 ==//

    /**
     * 주문 취소
     * @param orderId
     * @return
     */
    @Transactional
    public Mono<Void> cancel(Long orderId) {
        return customOrderRepository.findById(orderId)
                .flatMap(order -> updateOrderStatus(order)
                    .then(updateProductStockQuantity(order))
                    .then(updateDeliveryStatus(order))
                    .then(updatePaymentStatus(order))
                );
    }

    /**
     * 주문 상태 변경
     * @param order
     * @return
     */
    private Mono<Void> updateOrderStatus(Order order) {
        if(!order.getOrderStatus().equals(OrderStatus.COMPLETE)) {
            return Mono.error(new RuntimeException("주문이 완료되지 않았거나 이미 취소된 주문입니다."));
        }

        order.updateStatus(OrderStatus.CANCEL);

        return orderRepository.save(order).then();
    }

    /**
     * 취소한 만큼 상품 수량 업데이트
     * @param order
     * @return
     */
    private Mono<Void> updateProductStockQuantity(Order order) {
        return orderProductRepository.findAllByOrderId(order.getOrderId())
                .concatMap(orderProduct -> productRepository.findById(orderProduct.getProductId())
                        .flatMap(product -> {
                            int updatedStockQuantity = product.getStockQuantity() + orderProduct.getTotalCount();
                            product.updateStockQuantity(updatedStockQuantity);

                            return productRepository.save(product);
                        })
                ).then();
    }

    /**
     * 배송 상태 변경
     * @param order
     * @return
     */
    private Mono<Void> updateDeliveryStatus(Order order) {
        return customDeliveryRepository.findByOrderId(order.getOrderId())
                .flatMap(delivery -> {
                    delivery.updateStatus(DeliveryStatus.CANCEL);

                    return deliveryRepository.save(delivery);
                }).then();
    }

    /**
     * 결제 상태 변경
     * @param order
     * @return
     */
    private Mono<Void> updatePaymentStatus(Order order) {
        return customPaymentRepository.findByOrderId(order.getOrderId())
                .flatMap(payment -> {
                    payment.updateStatus(PaymentStatus.REFUND);

                    return paymentRepository.save(payment);
                }).then();
    }

    //== 주문 취소 로직 끝 ==//

}
