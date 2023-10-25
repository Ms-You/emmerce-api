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

    /**
     * 배송 상태 수정
     * @param deliveryId
     * @param statusReq
     * @return
     */
    public Mono<Void> changeStatus(Long deliveryId, DeliveryDTO.StatusReq statusReq) {
        return deliveryRepository.updateStatus(deliveryId, statusReq.getDeliveryStatus())
                .doOnNext(rowsUpdated ->
                        log.info("변경된 행 개수: {}", rowsUpdated)
                ).then();
    }

}
