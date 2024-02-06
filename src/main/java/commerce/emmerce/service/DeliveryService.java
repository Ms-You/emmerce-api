package commerce.emmerce.service;

import commerce.emmerce.dto.DeliveryDTO;
import commerce.emmerce.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    /**
     * 배송 상태 수정
     * @param orderProductId
     * @param statusReq
     * @return
     */
    public Mono<Void> changeStatus(Long orderProductId, DeliveryDTO.StatusReq statusReq) {
        return deliveryRepository.findByOrderProductId(orderProductId)
                .flatMap(delivery -> deliveryRepository.updateStatus(delivery.getDeliveryId(), delivery.getOrderProductId(), statusReq.getDeliveryStatus()))
                .then();
    }

}
