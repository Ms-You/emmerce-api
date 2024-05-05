package commerce.emmerce.controller.admin;

import commerce.emmerce.config.exception.GlobalErrorAttributes;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.service.CategoryProductService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser
@WebFluxTest(AdminCategoryProductController.class)
class AdminCategoryProductControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CategoryProductService categoryProductService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    @DisplayName("카테고리에 상품 추가 테스트")
    void enrollProductToCategory() {
        // given
        Long categoryId = 1L;
        Long productId = 1L;

        // when
        when(categoryProductService.enroll(anyLong(), anyLong())).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/admin/category/{categoryId}/product/{productId}", categoryId, productId)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(categoryProductService).enroll(anyLong(), anyLong());
    }

    @Test
    @DisplayName("카테고리에서 상품 제외 테스트")
    void deleteProductToCategory() {
        // given
        Long categoryId = 1L;
        Long productId = 1L;

        // when
        when(categoryProductService.cancel(anyLong(), anyLong())).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf())
                .delete()
                .uri("/admin/category/{categoryId}/product/{productId}", categoryId, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(categoryProductService).cancel(anyLong(), anyLong());
    }
}