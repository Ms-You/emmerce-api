package commerce.emmerce.service;

import commerce.emmerce.config.SecurityUtil;
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
    public Mono<OrderDTO.OrderResp> startOrder(OrderDTO.OrderReq orderReq) {
        return findCurrentMember()
                .flatMap(member -> makeOrder(member, orderReq));
    }

    /**
     * 주문 정보 생성
     * @param member
     * @param orderReq
     * @return
     */
    private Mono<OrderDTO.OrderResp> makeOrder(Member member, OrderDTO.OrderReq orderReq) {
        return orderRepository.save(Order.createOrder()
                        .orderDate(LocalDateTime.now())
                        .orderStatus(OrderStatus.ING)
                        .memberId(member.getMemberId())
                        .build())
                .doOnSuccess(savedOrder -> log.info("생성된 order_id: {}", savedOrder.getOrderId()))
                .flatMap(savedOrder -> saveProductsForOrder(savedOrder, orderReq.getOrderProductList())
                        .then(createDeliveryForOrder(savedOrder, orderReq.getDeliveryReq()))
                        .then(findOrderProducts(savedOrder)
                                .flatMap(orderProduct -> findProducts(orderProduct)
                                        .map(product -> OrderDTO.OrderProductResp.transfer(product, orderProduct))
                                ).collectList()
                                .map(orderProductRespList -> OrderDTO.OrderResp.transfer(savedOrder, orderProductRespList))
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
    //== 주문 생성 로직 끝 ==//

    /**
     * 주문 단건 조회
     * @param orderId
     * @return
     */
    public Mono<OrderDTO.OrderResp> getOrderInfo(Long orderId) {
        return orderRepository.findById(orderId)
                .flatMap(order -> findOrderProducts(order)
                        .flatMap(orderProduct -> findProducts(orderProduct)
                                .map(product -> OrderDTO.OrderProductResp.transfer(product, orderProduct))
                        ).collectList()
                        .map(orderProductRespList -> OrderDTO.OrderResp.transfer(order, orderProductRespList))
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
                        .flatMap(orderProduct -> findProducts(orderProduct)
                                .map(product -> OrderDTO.OrderProductResp.transfer(product, orderProduct))
                        ).collectList()
                        .map(orderProductRespList -> OrderDTO.OrderResp.transfer(order, orderProductRespList)
                        ));
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

}
