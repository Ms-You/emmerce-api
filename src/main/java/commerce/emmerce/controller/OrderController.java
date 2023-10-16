package commerce.emmerce.controller;

import commerce.emmerce.dto.OrderDTO;
import commerce.emmerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
@RequestMapping("/order")
@RestController
public class OrderController {

    private final OrderService orderService;

    /**
     * 상품 주문
     * @param orderReq
     * @return
     */
    @PostMapping
    public Mono<Void> orderProducts(@RequestBody OrderDTO.OrderReq orderReq) {
        return orderService.startOrder(orderReq);
    }


    /**
     * 주문 목록 조회
     * @return
     */
    @GetMapping
    public Flux<OrderDTO.OrderResp> orderList() {
        return orderService.getOrderList();
    }


    /**
     * 주문 취소
     * @param orderId
     * @return
     */
    @PutMapping("/{orderId}/cancel")
    public Mono<Void> orderCancel(@PathVariable Long orderId) {
        return orderService.cancel(orderId);
    }
}
