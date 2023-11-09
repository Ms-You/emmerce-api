package commerce.emmerce.kakaopay.controller;

import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.kakaopay.dto.KakaoPayDTO;
import commerce.emmerce.kakaopay.service.KakaoPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/payment")
@RestController
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;


    @PostMapping("/ready")
    public Mono<KakaoPayDTO.ReadyResp> readyToPay(@RequestBody KakaoPayDTO.PayReq payReq) {
        return kakaoPayService.kakaoPayReady(payReq);
    }


    @GetMapping("/success")
    public Mono<KakaoPayDTO.ApproveResp> success(@RequestParam("pg_token") String pgToken, @RequestParam("orderId") Long orderId) {
        return kakaoPayService.kakaoPayApprove(pgToken, orderId);
    }


    @PostMapping("/order")
    public Mono<KakaoPayDTO.OrderResp> lookUpOrder(@RequestBody KakaoPayDTO.PayReq payReq) {
        return kakaoPayService.kakaoPayOrdered(payReq);
    }


    @GetMapping("/cancel")
    public Mono<ResponseEntity> cancel() {
        return Mono.error(new GlobalException(ErrorCode.PAYMENT_CANCELED));
    }


    @GetMapping("/fail")
    public Mono<ResponseEntity> fail() {
        return Mono.error(new GlobalException(ErrorCode.PAYMENT_FAILED));
    }


    @PostMapping("/refund")
    public Mono<KakaoPayDTO.CancelResp> refund(@RequestBody KakaoPayDTO.PayReq payReq) {
        return kakaoPayService.kakaoPayCancel(payReq);
    }

}
