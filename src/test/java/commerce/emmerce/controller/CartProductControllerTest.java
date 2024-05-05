package commerce.emmerce.controller;

import commerce.emmerce.config.exception.GlobalErrorAttributes;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.dto.CartProductDTO;
import commerce.emmerce.service.CartProductService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser
@WebFluxTest(CartProductController.class)
class CartProductControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartProductService cartProductService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    @DisplayName("장바구니에 상품 넣기 테스트")
    void putProductInCart() {
        // given
        CartProductDTO.EnrollReq enrollReq = new CartProductDTO.EnrollReq(1L, 10);

        // when
        webTestClient.mutateWith(csrf())
                .post()
                .uri("/cart/product")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(enrollReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(cartProductService).putInCart(any(CartProductDTO.EnrollReq.class));
    }

    @Test
    @DisplayName("장바구니에 속한 상품 목록 조회 테스트")
    void cartProductList() {
        // given
        CartProductDTO.ListResp listResp1 = CartProductDTO.ListResp.builder()
                .cartProductId(1L)
                .productId(1L)
                .name("샴푸")
                .titleImg("샴푸 이미지")
                .originalPrice(10000)
                .discountPrice(8000)
                .quantity(5)
                .totalPrice(40000)
                .brand("에비앙")
                .build();

        CartProductDTO.ListResp listResp2 = CartProductDTO.ListResp.builder()
                .cartProductId(2L)
                .productId(2L)
                .name("린스")
                .titleImg("린스 이미지")
                .originalPrice(8000)
                .discountPrice(6000)
                .quantity(5)
                .totalPrice(30000)
                .brand("삼다수")
                .build();

        // when
        when(cartProductService.productList()).thenReturn(Flux.just(listResp1, listResp2));

        webTestClient
                .get()
                .uri("/cart/product")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].cartProductId").isEqualTo(listResp1.getCartProductId())
                .jsonPath("$[0].productId").isEqualTo(listResp1.getProductId())
                .jsonPath("$[0].name").isEqualTo(listResp1.getName())
                .jsonPath("$[0].titleImg").isEqualTo(listResp1.getTitleImg())
                .jsonPath("$[0].originalPrice").isEqualTo(listResp1.getOriginalPrice())
                .jsonPath("$[0].discountPrice").isEqualTo(listResp1.getDiscountPrice())
                .jsonPath("$[0].quantity").isEqualTo(listResp1.getQuantity())
                .jsonPath("$[0].totalPrice").isEqualTo(listResp1.getTotalPrice())
                .jsonPath("$[0].brand").isEqualTo(listResp1.getBrand())
                .jsonPath("$[1].cartProductId").isEqualTo(listResp2.getCartProductId())
                .jsonPath("$[1].productId").isEqualTo(listResp2.getProductId())
                .jsonPath("$[1].name").isEqualTo(listResp2.getName())
                .jsonPath("$[1].titleImg").isEqualTo(listResp2.getTitleImg())
                .jsonPath("$[1].originalPrice").isEqualTo(listResp2.getOriginalPrice())
                .jsonPath("$[1].discountPrice").isEqualTo(listResp2.getDiscountPrice())
                .jsonPath("$[1].quantity").isEqualTo(listResp2.getQuantity())
                .jsonPath("$[1].totalPrice").isEqualTo(listResp2.getTotalPrice())
                .jsonPath("$[1].brand").isEqualTo(listResp2.getBrand());

        // then
        verify(cartProductService).productList();
    }

    @Test
    @DisplayName("장바구니에 속한 상품 삭제 테스트")
    void removeProductInCart() {
        // given
        Long cartProductId = 1L;

        // when
        webTestClient.mutateWith(csrf())
                .delete()
                .uri("/cart/{cartProductId}", cartProductId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(cartProductService).removeInCart(anyLong());
    }

    @Test
    @DisplayName("장바구니 비우기 테스트")
    void clearCart() {
        // given

        // when
        webTestClient.mutateWith(csrf())
                .delete()
                .uri("/cart/clear")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(cartProductService).clear();
    }
}