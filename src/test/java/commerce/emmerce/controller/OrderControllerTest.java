package commerce.emmerce.controller;

import commerce.emmerce.config.exception.GlobalErrorAttributes;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.domain.DeliveryStatus;
import commerce.emmerce.domain.OrderStatus;
import commerce.emmerce.dto.DeliveryDTO;
import commerce.emmerce.dto.OrderDTO;
import commerce.emmerce.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser
@WebFluxTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    @DisplayName("상품 주문 테스트")
    void orderProducts() {
        // given
        OrderDTO.OrderProductReq orderProductReq1 = new OrderDTO.OrderProductReq(1L, 3);
        OrderDTO.OrderProductReq orderProductReq2 = new OrderDTO.OrderProductReq(2L, 5);
        DeliveryDTO.DeliveryReq deliveryReq = DeliveryDTO.DeliveryReq.builder()
                .name("tester001")
                .tel("01012345678")
                .email("test@test.com")
                .city("경기도 오산시")
                .street("공룡로 49")
                .zipcode("18888")
                .build();

        OrderDTO.OrderReq orderReq = new OrderDTO.OrderReq(List.of(orderProductReq1, orderProductReq2), deliveryReq);

        OrderDTO.OrderCreateResp orderCreateResp = new OrderDTO.OrderCreateResp(1L);

        // when
        when(orderService.startOrder(any(OrderDTO.OrderReq.class))).thenReturn(Mono.just(orderCreateResp));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(orderReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.orderId").isEqualTo(orderCreateResp.getOrderId());

        // then
        verify(orderService).startOrder(any(OrderDTO.OrderReq.class));
    }

    @Test
    @DisplayName("주문 내역 단건 조회 테스트")
    void findOrder() {
        // given
        Long orderId = 1L;

        OrderDTO.OrderProductResp orderProductResp1 = OrderDTO.OrderProductResp.builder()
                .orderProductId(1L)
                .productId(1L)
                .name("샴푸")
                .titleImg("샴푸 이미지")
                .brand("에비앙")
                .originalPrice(10000)
                .discountPrice(8000)
                .quantity(5)
                .reviewStatus(false)
                .deliveryStatus(DeliveryStatus.ING)
                .build();

        OrderDTO.OrderProductResp orderProductResp2 = OrderDTO.OrderProductResp.builder()
                .orderProductId(2L)
                .productId(2L)
                .name("린스")
                .titleImg("린스 이미지")
                .brand("삼다수")
                .originalPrice(8000)
                .discountPrice(6000)
                .quantity(3)
                .reviewStatus(false)
                .deliveryStatus(DeliveryStatus.ING)
                .build();

        OrderDTO.OrderResp orderResp = OrderDTO.OrderResp.builder()
                .orderId(orderId)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETE)
                .orderProductRespList(List.of(orderProductResp1, orderProductResp2))
                .build();

        // when
        when(orderService.getOrderInfo(anyLong())).thenReturn(Mono.just(orderResp));

        webTestClient
                .get()
                .uri("/order/{orderId}", orderId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.orderId").isEqualTo(orderResp.getOrderId())
                .jsonPath("$.orderDate").exists()
                .jsonPath("$.orderStatus").isEqualTo(orderResp.getOrderStatus().name())
                .jsonPath("$.orderProductRespList.[0].orderProductId").isEqualTo(orderResp.getOrderProductRespList().get(0).getOrderProductId())
                .jsonPath("$.orderProductRespList.[0].productId").isEqualTo(orderResp.getOrderProductRespList().get(0).getProductId())
                .jsonPath("$.orderProductRespList.[0].name").isEqualTo(orderResp.getOrderProductRespList().get(0).getName())
                .jsonPath("$.orderProductRespList.[0].titleImg").isEqualTo(orderResp.getOrderProductRespList().get(0).getTitleImg())
                .jsonPath("$.orderProductRespList.[0].brand").isEqualTo(orderResp.getOrderProductRespList().get(0).getBrand())
                .jsonPath("$.orderProductRespList.[0].originalPrice").isEqualTo(orderResp.getOrderProductRespList().get(0).getOriginalPrice())
                .jsonPath("$.orderProductRespList.[0].discountPrice").isEqualTo(orderResp.getOrderProductRespList().get(0).getDiscountPrice())
                .jsonPath("$.orderProductRespList.[0].quantity").isEqualTo(orderResp.getOrderProductRespList().get(0).getQuantity())
                .jsonPath("$.orderProductRespList.[0].reviewStatus").isEqualTo(orderResp.getOrderProductRespList().get(0).isReviewStatus())
                .jsonPath("$.orderProductRespList.[0].deliveryStatus").isEqualTo(orderResp.getOrderProductRespList().get(0).getDeliveryStatus().name())
                .jsonPath("$.orderProductRespList.[1].orderProductId").isEqualTo(orderResp.getOrderProductRespList().get(1).getOrderProductId())
                .jsonPath("$.orderProductRespList.[1].productId").isEqualTo(orderResp.getOrderProductRespList().get(1).getProductId())
                .jsonPath("$.orderProductRespList.[1].name").isEqualTo(orderResp.getOrderProductRespList().get(1).getName())
                .jsonPath("$.orderProductRespList.[1].titleImg").isEqualTo(orderResp.getOrderProductRespList().get(1).getTitleImg())
                .jsonPath("$.orderProductRespList.[1].brand").isEqualTo(orderResp.getOrderProductRespList().get(1).getBrand())
                .jsonPath("$.orderProductRespList.[1].originalPrice").isEqualTo(orderResp.getOrderProductRespList().get(1).getOriginalPrice())
                .jsonPath("$.orderProductRespList.[1].discountPrice").isEqualTo(orderResp.getOrderProductRespList().get(1).getDiscountPrice())
                .jsonPath("$.orderProductRespList.[1].quantity").isEqualTo(orderResp.getOrderProductRespList().get(1).getQuantity())
                .jsonPath("$.orderProductRespList.[1].reviewStatus").isEqualTo(orderResp.getOrderProductRespList().get(1).isReviewStatus())
                .jsonPath("$.orderProductRespList.[1].deliveryStatus").isEqualTo(orderResp.getOrderProductRespList().get(1).getDeliveryStatus().name());

        // then
        verify(orderService).getOrderInfo(anyLong());
    }

    @Test
    @DisplayName("주문 내역 전체 조회 테스트")
    void orderList() {
        // given
        OrderDTO.OrderProductResp orderProductResp1 = OrderDTO.OrderProductResp.builder()
                .orderProductId(1L)
                .productId(1L)
                .name("샴푸")
                .titleImg("샴푸 이미지")
                .brand("에비앙")
                .originalPrice(10000)
                .discountPrice(8000)
                .quantity(5)
                .reviewStatus(false)
                .deliveryStatus(DeliveryStatus.ING)
                .build();

        OrderDTO.OrderProductResp orderProductResp2 = OrderDTO.OrderProductResp.builder()
                .orderProductId(1L)
                .productId(1L)
                .name("린스")
                .titleImg("린스 이미지")
                .brand("삼다수")
                .originalPrice(8000)
                .discountPrice(6000)
                .quantity(3)
                .reviewStatus(false)
                .deliveryStatus(DeliveryStatus.ING)
                .build();

        OrderDTO.OrderResp orderResp = OrderDTO.OrderResp.builder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETE)
                .orderProductRespList(List.of(orderProductResp1, orderProductResp2))
                .build();

        // when
        when(orderService.getOrderList()).thenReturn(Flux.just(orderResp));

        webTestClient
                .get()
                .uri("/order")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].orderId").isEqualTo(orderResp.getOrderId())
                .jsonPath("$[0].orderDate").exists()
                .jsonPath("$[0].orderStatus").isEqualTo(orderResp.getOrderStatus().name())
                .jsonPath("$[0].orderProductRespList.[0].orderProductId").isEqualTo(orderResp.getOrderProductRespList().get(0).getOrderProductId())
                .jsonPath("$[0].orderProductRespList.[0].productId").isEqualTo(orderResp.getOrderProductRespList().get(0).getProductId())
                .jsonPath("$[0].orderProductRespList.[0].name").isEqualTo(orderResp.getOrderProductRespList().get(0).getName())
                .jsonPath("$[0].orderProductRespList.[0].titleImg").isEqualTo(orderResp.getOrderProductRespList().get(0).getTitleImg())
                .jsonPath("$[0].orderProductRespList.[0].brand").isEqualTo(orderResp.getOrderProductRespList().get(0).getBrand())
                .jsonPath("$[0].orderProductRespList.[0].originalPrice").isEqualTo(orderResp.getOrderProductRespList().get(0).getOriginalPrice())
                .jsonPath("$[0].orderProductRespList.[0].discountPrice").isEqualTo(orderResp.getOrderProductRespList().get(0).getDiscountPrice())
                .jsonPath("$[0].orderProductRespList.[0].quantity").isEqualTo(orderResp.getOrderProductRespList().get(0).getQuantity())
                .jsonPath("$[0].orderProductRespList.[0].reviewStatus").isEqualTo(orderResp.getOrderProductRespList().get(0).isReviewStatus())
                .jsonPath("$[0].orderProductRespList.[0].deliveryStatus").isEqualTo(orderResp.getOrderProductRespList().get(0).getDeliveryStatus().name())
                .jsonPath("$[0].orderProductRespList.[1].orderProductId").isEqualTo(orderResp.getOrderProductRespList().get(1).getOrderProductId())
                .jsonPath("$[0].orderProductRespList.[1].productId").isEqualTo(orderResp.getOrderProductRespList().get(1).getProductId())
                .jsonPath("$[0].orderProductRespList.[1].name").isEqualTo(orderResp.getOrderProductRespList().get(1).getName())
                .jsonPath("$[0].orderProductRespList.[1].titleImg").isEqualTo(orderResp.getOrderProductRespList().get(1).getTitleImg())
                .jsonPath("$[0].orderProductRespList.[1].brand").isEqualTo(orderResp.getOrderProductRespList().get(1).getBrand())
                .jsonPath("$[0].orderProductRespList.[1].originalPrice").isEqualTo(orderResp.getOrderProductRespList().get(1).getOriginalPrice())
                .jsonPath("$[0].orderProductRespList.[1].discountPrice").isEqualTo(orderResp.getOrderProductRespList().get(1).getDiscountPrice())
                .jsonPath("$[0].orderProductRespList.[1].quantity").isEqualTo(orderResp.getOrderProductRespList().get(1).getQuantity())
                .jsonPath("$[0].orderProductRespList.[1].reviewStatus").isEqualTo(orderResp.getOrderProductRespList().get(1).isReviewStatus())
                .jsonPath("$[0].orderProductRespList.[1].deliveryStatus").isEqualTo(orderResp.getOrderProductRespList().get(1).getDeliveryStatus().name());

        // then

    }
}