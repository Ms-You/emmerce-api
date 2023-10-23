package commerce.emmerce.controller;

import commerce.emmerce.dto.DeliveryDTO;
import commerce.emmerce.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/delivery")
@RestController
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * 배달 상태 수정 (관리자)
     * @param deliveryId
     * @param statusReq
     * @return
     */
    @PutMapping("/{deliveryId}")
    public Mono<ResponseEntity> updateDeliveryStatus(@PathVariable Long deliveryId,
                                                     @RequestBody DeliveryDTO.StatusReq statusReq) {
        return deliveryService.changeStatus(deliveryId, statusReq)
                .then(Mono.just(new ResponseEntity(HttpStatus.OK)))
                .onErrorReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));
    }



}
