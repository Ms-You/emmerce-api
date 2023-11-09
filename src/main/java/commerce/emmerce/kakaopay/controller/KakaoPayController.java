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
    public Mono<KakaoPayDTO.ReadyResp> readyToPay(@RequestBody KakaoPayDTO.ReadyReq readyReq) {
        return kakaoPayService.kakaoPayReady(readyReq);
    }


    @GetMapping("/success")
    public Mono<KakaoPayDTO.ApproveResp> success(@RequestParam("pg_token") String pgToken) {
        return kakaoPayService.kakaoPayApprove(pgToken);
    }


    @GetMapping("/cancel")
    public Mono<ResponseEntity> cancel() {
        return Mono.error(new GlobalException(ErrorCode.PAYMENT_CANCELED));
    }


    @GetMapping("/fail")
    public Mono<ResponseEntity> fail() {
        return Mono.error(new GlobalException(ErrorCode.PAYMENT_FAILED));
    }

}
