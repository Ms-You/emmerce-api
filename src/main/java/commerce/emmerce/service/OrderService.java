package commerce.emmerce.service;

import commerce.emmerce.config.SecurityUtil;
import commerce.emmerce.domain.Order;
import commerce.emmerce.domain.OrderProduct;
import commerce.emmerce.domain.OrderStatus;
import commerce.emmerce.dto.OrderDTO;
import commerce.emmerce.repository.MemberRepositoryImpl;
import commerce.emmerce.repository.OrderProductRepositoryImpl;
import commerce.emmerce.repository.OrderRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final MemberRepositoryImpl memberRepository;
    private final OrderRepositoryImpl orderRepository;
    private final OrderProductRepositoryImpl orderProductRepository;


    public Mono<Void> createOrder(List<OrderDTO.OrderCartProductReq> orderCartProductReqList) {
        return SecurityUtil.getCurrentMemberName()
                .flatMap(name -> memberRepository.findByName(name)
                        .flatMap(member -> orderRepository.save(Order.createOrder()
                                        .orderDate(LocalDateTime.now())
                                        .orderStatus(OrderStatus.ING)
                                        .memberId(member.getMemberId())
                                        .build())
                                .flatMap(order -> Flux.fromIterable(orderCartProductReqList)
                                        .flatMap(orderCartProductDTO -> orderProductRepository.save(OrderProduct.builder()
                                                .totalPrice(orderCartProductDTO.getTotalPrice())
                                                .totalCount(orderCartProductDTO.getTotalCount())
                                                .orderId(order.getOrderId())
                                                .productId(orderCartProductDTO.getProductId())
                                                .build()))
                                        .then()
                                )
                        )
                );
    }


}
