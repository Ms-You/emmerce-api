package commerce.emmerce.controller;

import commerce.emmerce.dto.PaymentDTO;
import commerce.emmerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/payment")
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public Mono<ResponseEntity<PaymentDTO.PaymentResp>> makePayment(@RequestBody PaymentDTO.PaymentReq paymentReq) {
        return paymentService.process(paymentReq)
                .map(resp -> ResponseEntity.ok(resp))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }



    @GetMapping("/{paymentId}")
    public Mono<ResponseEntity<PaymentDTO.PaymentResp>> getDetails(@PathVariable Long paymentId) {
        return paymentService.details(paymentId)
                .map(resp -> ResponseEntity.ok(resp))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }



}
