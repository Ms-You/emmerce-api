package commerce.emmerce.controller.admin;

import commerce.emmerce.config.exception.GlobalErrorAttributes;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.domain.DeliveryStatus;
import commerce.emmerce.dto.DeliveryDTO;
import commerce.emmerce.service.DeliveryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser
@WebFluxTest(AdminDeliveryController.class)
class AdminDeliveryControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DeliveryService deliveryService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    @DisplayName("배송 상태 수정 테스트")
    void updateDeliveryStatus() {
        // given
        Long orderProductId = 1L;
        DeliveryDTO.StatusReq statusReq = new DeliveryDTO.StatusReq(DeliveryStatus.COMPLETE);

        // when
        when(deliveryService.changeStatus(anyLong(), any(DeliveryDTO.StatusReq.class))).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf())
                .put()
                .uri("/admin/delivery/orderProduct/{orderProductId}", orderProductId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(deliveryService).changeStatus(anyLong(), any(DeliveryDTO.StatusReq.class));
    }
}