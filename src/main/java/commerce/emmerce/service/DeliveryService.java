package commerce.emmerce.service;

import commerce.emmerce.dto.DeliveryDTO;
import commerce.emmerce.repository.DeliveryRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeliveryService {

    private final DeliveryRepositoryImpl deliveryRepository;

    public Mono<Long> changeStatus(Long deliveryId, DeliveryDTO.DeliveryStatusReq deliveryStatusReq) {
        return deliveryRepository.updateStatus(deliveryId, deliveryStatusReq.getDeliveryStatus())
                .doOnNext(rowsUpdated ->
                        log.info("변경된 행 개수: {}", rowsUpdated)
                );
    }


}
