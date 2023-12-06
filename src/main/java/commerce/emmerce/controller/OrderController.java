package commerce.emmerce.controller;

import commerce.emmerce.dto.OrderDTO;
import commerce.emmerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Order", description = "주문 관련 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/order")
@RestController
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "상품 주문", description = "현재 로그인 한 사용자에 대해 새로운 주문 정보를 생성합니다.")
    @Parameter(name = "orderReq", description = "주문할 상품 목록, 배송 정보, 결제 정보")
    @PostMapping
    public Mono<OrderDTO.OrderResp> orderProducts(@RequestBody OrderDTO.OrderReq orderReq) {
        return orderService.startOrder(orderReq);
    }


    @Operation(summary = "주문 내역 단건 조회", description = "현재 로그인 한 사용자의 주문 내역 단건을 조회합니다.")
    @Parameter(name = "oderId", description = "조회할 주문 id")
    @GetMapping("/{orderId}")
    public Mono<OrderDTO.OrderResp> findOrder(@PathVariable Long orderId) {
        return orderService.getOrderInfo(orderId);
    }



    @Operation(summary = "주문 내역 전체 조회", description = "현재 로그인 한 사용자의 주문 내역 전체를 조회합니다.")
    @GetMapping
    public Flux<OrderDTO.OrderResp> orderList() {
        return orderService.getOrderList();
    }
}
