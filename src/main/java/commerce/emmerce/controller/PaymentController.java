package commerce.emmerce.controller;

import commerce.emmerce.dto.PaymentDTO;
import commerce.emmerce.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "Payment", description = "결제 관련 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/payment")
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 정보 조회", description = "현재 로그인 한 사용자의 결제 내역을 조회합니다.")
    @Parameter(name = "paymentId", description = "조회할 결제 id")
    @GetMapping("/{paymentId}")
    public Mono<PaymentDTO.PaymentResp> getDetails(@PathVariable Long paymentId) {
        return paymentService.details(paymentId);
    }

}
