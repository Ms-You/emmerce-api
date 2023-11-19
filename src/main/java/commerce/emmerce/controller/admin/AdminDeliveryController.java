package commerce.emmerce.controller.admin;

import commerce.emmerce.dto.DeliveryDTO;
import commerce.emmerce.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "AdminDelivery", description = "배송 관련 컨트롤러 (관리자)")
@RequiredArgsConstructor
@RequestMapping("/admin/delivery")
@RestController
public class AdminDeliveryController {

    private final DeliveryService deliveryService;

    @Operation(summary = "배달 상태 수정", description = "배송 상태를 수정합니다. (READY, ING, COMPLETE, CANCEL)")
    @Parameters({ @Parameter(name = "deliveryId", description = "조회할 배송 id"),
                @Parameter(name = "statusReq", description = "변경할 배송 상태") })
    @PutMapping("/{deliveryId}")
    public Mono<Void> updateDeliveryStatus(@PathVariable Long deliveryId,
                                                     @RequestBody DeliveryDTO.StatusReq statusReq) {
        return deliveryService.changeStatus(deliveryId, statusReq);
    }
}