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


    @PostMapping
    public Mono<ResponseEntity> createDelivery(@RequestBody DeliveryDTO.DeliveryReq deliveryReq) {
        return deliveryService.deliveryStart(deliveryReq)
                .then(Mono.just(new ResponseEntity(HttpStatus.CREATED)))
                .onErrorReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));
    }


    @PutMapping("/{deliveryId}")
    public Mono<ResponseEntity> updateDeliveryStatus(@PathVariable Long deliveryId,
                                                     @RequestBody DeliveryDTO.DeliveryStatusReq deliveryStatusReq) {
        return deliveryService.changeStatus(deliveryId, deliveryStatusReq)
                .then(Mono.just(new ResponseEntity(HttpStatus.OK)))
                .onErrorReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));
    }



}
