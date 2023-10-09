package commerce.emmerce.controller;

import commerce.emmerce.dto.OrderDTO;
import commerce.emmerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/order")
@RestController
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Mono<ResponseEntity> orderProducts(@RequestBody List<OrderDTO.OrderCartProductReq> orderCartProductReqList) {
        return orderService.createOrder(orderCartProductReqList)
                .then(Mono.just(new ResponseEntity(HttpStatus.CREATED)))
                .onErrorReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));
    }


}
