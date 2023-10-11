package commerce.emmerce.controller;

import commerce.emmerce.dto.OrderDTO;
import commerce.emmerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RequiredArgsConstructor
@RequestMapping("/order")
@RestController
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Mono<ResponseEntity> orderProducts(@RequestBody OrderDTO.OrderReq orderReq) {
        return orderService.startOrder(orderReq)
                .then(Mono.just(new ResponseEntity(HttpStatus.CREATED)))
                .onErrorReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));
    }


    @GetMapping
    public Flux<OrderDTO.OrderResp> orderList() {
        return orderService.getOrderList();
    }

}
