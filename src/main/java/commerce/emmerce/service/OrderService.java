package commerce.emmerce.service;

import commerce.emmerce.config.SecurityUtil;
import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.domain.*;
import commerce.emmerce.dto.DeliveryDTO;
import commerce.emmerce.dto.OrderDTO;
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

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final DeliveryRepository deliveryRepository;
    private final ReviewRepository reviewRepository;

    private Mono<Member> findCurrentMember() {
        return SecurityUtil.getCurrentMemberName()
                .flatMap(name -> memberRepository.findByName(name));
    }

    //== 주문 생성 로직 시작 ==//
    /**
     * 주문 시작
     * @param orderReq
     * @return
     */
    @Transactional
    public Mono<OrderDTO.OrderCreateResp> startOrder(OrderDTO.OrderReq orderReq) {
        return findCurrentMember()
                .flatMap(member -> makeOrder(member, orderReq));
    }

    /**
     * 주문 정보 생성
     * @param member
     * @param orderReq
     * @return
     */
    private Mono<OrderDTO.OrderCreateResp> makeOrder(Member member, OrderDTO.OrderReq orderReq) {
        return orderRepository.save(Order.createOrder()
                        .orderDate(LocalDateTime.now())
                        .orderStatus(OrderStatus.ING)
                        .memberId(member.getMemberId())
                        .build())
                .doOnSuccess(savedOrder -> log.info("생성된 order_id: {}", savedOrder.getOrderId()))
                .flatMap(savedOrder -> saveProductsForOrder(savedOrder, orderReq.getOrderProductList())
                        .then(orderProductRepository.findAllByOrderId(savedOrder.getOrderId())
                                .flatMap(orderProduct -> createDeliveryForOrder(orderReq.getDeliveryReq(), orderProduct.getOrderProductId()))
                                .then())
                        .then(
                                Mono.just(new OrderDTO.OrderCreateResp(savedOrder.getOrderId()))
                        )
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
                .concatMap(orderProductReq -> productRepository.findById(orderProductReq.getProductId())
                        .flatMap(product -> {
                            if(product.getStockQuantity() < orderProductReq.getTotalCount()) {
                                return Mono.error(new RuntimeException("'" + product.getName() + "' 상품의 재고가 부족합니다."));
                            } else {
                                return orderProductRepository.save(OrderProduct.builder()
                                            .totalPrice(product.getDiscountPrice() * orderProductReq.getTotalCount())   // 각 상품의 구매 금액
                                            .totalCount(orderProductReq.getTotalCount())
                                            .orderId(order.getOrderId())
                                            .productId(orderProductReq.getProductId())
                                            .build());
                            }
                        })
                ).then();
    }

    /**
     * 배송 정보 추가
     * @param deliveryReq
     * @param orderProductId
     * @return
     */
    private Mono<Void> createDeliveryForOrder(DeliveryDTO.DeliveryReq deliveryReq, Long orderProductId) {
        return deliveryRepository.save(Delivery.createDelivery()
                .name(deliveryReq.getName())
                .tel(deliveryReq.getTel())
                .email(deliveryReq.getEmail())
                .city(deliveryReq.getCity())
                .street(deliveryReq.getStreet())
                .zipcode(deliveryReq.getZipcode())
                .deliveryStatus(DeliveryStatus.READY)
                .orderProductId(orderProductId)
                .build());
    }
    //== 주문 생성 로직 끝 ==//

    /**
     * 주문 단건 조회
     * @param orderId
     * @return
     */
    public Mono<OrderDTO.OrderResp> getOrderInfo(Long orderId) {
        return findCurrentMember()
                .flatMap(member -> orderRepository.findById(orderId)
                        .flatMap(order -> {
                            // 현재 사용자가 주문자와 같은지 체크
                            if (order.getMemberId() != member.getMemberId()) {
                                return Mono.error(new GlobalException(ErrorCode.ORDER_MEMBER_NOT_MATCHED));
                            }

                            return findOrderProducts(order)
                                    .flatMap(orderProduct -> Mono.zip(
                                            findProducts(orderProduct),
                                            deliveryRepository.findByOrderProductId(orderProduct.getOrderProductId())
                                                    .switchIfEmpty(Mono.error(new GlobalException(ErrorCode.DELIVERY_NOT_FOUND_BY_ORDER_PRODUCT))),
                                            checkReviewWrote(member, orderProduct.getOrderProductId())
                                            // 상품 목록, 배송 정보, 리뷰 작성 여부
                                    ).flatMap(tuple -> {
                                        Product product = tuple.getT1();
                                        Delivery delivery = tuple.getT2();
                                        Boolean reviewStatus = tuple.getT3();
                                        DeliveryStatus deliveryStatus = delivery != null ? delivery.getDeliveryStatus() : DeliveryStatus.READY;

                                        return Mono.just(OrderDTO.OrderProductResp.transfer(
                                                product,
                                                orderProduct,
                                                reviewStatus,
                                                deliveryStatus
                                        ));
                                    }).onErrorResume(e -> {
                                        log.error(e.getMessage());

                                        return Mono.empty();
                                    })).collectList()
                                    .map(orderProductRespList -> OrderDTO.OrderResp.transfer(order, orderProductRespList));
                        })
                );
    }

    //== 주문 전체 조회 로직 시작 ==//
    /**
     * 주문 목록 조회
     * @return
     */
    @Transactional
    public Flux<OrderDTO.OrderResp> getOrderList() {
        return findCurrentMember()
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
                        .flatMap(orderProduct -> Mono.zip(
                                        findProducts(orderProduct),
                                        deliveryRepository.findByOrderProductId(orderProduct.getOrderProductId())
                                                .switchIfEmpty(Mono.error(new GlobalException(ErrorCode.DELIVERY_NOT_FOUND_BY_ORDER_PRODUCT))),
                                        checkReviewWrote(member, orderProduct.getOrderProductId()))
                                .map(tuple -> {
                                    Product product = tuple.getT1();
                                    Delivery delivery = tuple.getT2();
                                    Boolean reviewStatus = tuple.getT3();
                                    DeliveryStatus deliveryStatus = delivery != null ? delivery.getDeliveryStatus() : DeliveryStatus.READY;

                                    return OrderDTO.OrderProductResp.transfer(
                                            product,
                                            orderProduct,
                                            reviewStatus,
                                            deliveryStatus
                                    );
                                }).onErrorResume(e -> {
                                    log.error(e.getMessage());

                                    return Mono.empty();
                                })
                        ).collectList()
                        .map(orderProductRespList -> OrderDTO.OrderResp.transfer(order, orderProductRespList))
                )
                .onErrorResume(e -> {
                    log.error(e.getMessage());

                    return Flux.empty();
                });
    }

    /**
     * 주문 상품 목록 조회
     * @param order
     * @return
     */
    public Flux<OrderProduct> findOrderProducts(Order order) {
        return orderProductRepository.findAllByOrderId(order.getOrderId());
    }

    /**
     * 상품 정보 조회
     * @param orderProduct
     * @return
     */
    public Mono<Product> findProducts(OrderProduct orderProduct) {
        return productRepository.findById(orderProduct.getProductId());
    }
    //== 주문 전체 조회 로직 끝 ==//

    /**
     * 리뷰 작성 여부 조회
     * @param member
     * @param orderProductId
     * @return
     */
    public Mono<Boolean> checkReviewWrote(Member member, Long orderProductId) {
        return reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), orderProductId)
                .map(count -> count != 0);
    }

}
