package commerce.emmerce.service;

import commerce.emmerce.domain.Delivery;
import commerce.emmerce.domain.DeliveryStatus;
import commerce.emmerce.dto.DeliveryDTO;
import commerce.emmerce.repository.DeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(DeliveryService.class)
class DeliveryServiceTest {
    @Autowired
    private DeliveryService deliveryService;

    @MockBean
    private DeliveryRepository deliveryRepository;

    private Delivery delivery;

    @BeforeEach
    void setup() {
        delivery = Delivery.createDelivery()
                .deliveryId(1L)
                .name("tester001")
                .tel("01012345678")
                .email("test@test.com")
                .city("서울특별시")
                .street("공룡로 50")
                .zipcode("18888")
                .deliveryStatus(DeliveryStatus.READY)
                .orderProductId(1L)
                .build();
    }

    @Test
    @DisplayName("배송 상태 수정 테스트")
    void changeStatus() {
        // given
        DeliveryDTO.StatusReq statusReq = new DeliveryDTO.StatusReq(DeliveryStatus.READY);

        // when
        when(deliveryRepository.findByOrderProductId(delivery.getOrderProductId())).thenReturn(Mono.just(delivery));
        when(deliveryRepository.updateStatus(delivery.getDeliveryId(), delivery.getOrderProductId(), delivery.getDeliveryStatus())).thenReturn(Mono.empty());

        StepVerifier.create(deliveryService.changeStatus(delivery.getOrderProductId(), statusReq))
                .verifyComplete();

        // then
        verify(deliveryRepository, times(1)).findByOrderProductId(anyLong());
        verify(deliveryRepository, times(1)).updateStatus(anyLong(), anyLong(), any(DeliveryStatus.class));
    }
}