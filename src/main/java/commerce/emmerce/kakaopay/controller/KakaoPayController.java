package commerce.emmerce.kakaopay.controller;

import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.kakaopay.dto.KakaoPayDTO;
import commerce.emmerce.kakaopay.service.KakaoPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "Payment", description = "결제 관련 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/payment")
@RestController
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;


    @Operation(summary = "결제 준비 요청", description = "주문 정보를 바탕으로 카카오페이 서버에 결제 준비 요청을 합니다.")
    @Parameter(name = "payReq", description = "결제할 주문 id")
    @PostMapping("/ready")
    public Mono<KakaoPayDTO.ReadyResp> readyToPay(@RequestBody KakaoPayDTO.PayReq payReq) {
        return kakaoPayService.kakaoPayReady(payReq);
    }


    @Operation(summary = "결제 승인 요청", description = "결제 준비 요청 후 응답 받은 pgToken과 주문 정보를 바탕으로 결제 승인 요청을 합니다.")
    @Parameters({ @Parameter(name = "pg_token", description = "결제 준비 요청으로 응답 받은 token"),
                @Parameter(name = "orderId", description = "결제할 주문 id") })
    @GetMapping("/success")
    public Mono<KakaoPayDTO.ApproveResp> success(@RequestParam("pg_token") String pgToken, @RequestParam("orderId") Long orderId) {
        return kakaoPayService.kakaoPayApprove(pgToken, orderId);
    }


    @Operation(summary = "결제 정보 조회 요청", description = "결제한 정보를 조회합니다.")
    @Parameter(name = "payReq", description = "결제 정보를 조회할 주문 id")
    @PostMapping("/order")
    public Mono<KakaoPayDTO.OrderResp> lookUpOrder(@RequestBody KakaoPayDTO.PayReq payReq) {
        return kakaoPayService.kakaoPayInfo(payReq);
    }


    @Operation(summary = "결제 취소", description = "결제 요청 중 사용자 요청으로 결제를 취소합니다.")
    @PostMapping("/cancel")
    public Mono<Void> cancel(@RequestBody KakaoPayDTO.PayReq payReq) {
        return kakaoPayService.kakaoPayCancel(payReq);
    }


    @Operation(summary = "결제 실패", description = "결제 요청 중 에러 발생으로 결제 실패 시 리다이렉트합니다.")
    @GetMapping("/fail")
    public Mono<ResponseEntity> fail() {
        return Mono.error(new GlobalException(ErrorCode.PAYMENT_FAILED));
    }


    /*@Operation(summary = "결제 환불 요청", description = "결제 승인 완료 후 결제에 대해 환불합니다.")
    @Parameter(name = "payReq", description = "환불 할 주문 id")
    @PostMapping("/refund")
    public Mono<KakaoPayDTO.RefundResp> refund(@RequestBody KakaoPayDTO.PayReq payReq) {
        return kakaoPayService.kakaoPayRefund(payReq);
    }*/

}
