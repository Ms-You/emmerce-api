package commerce.emmerce.controller.admin;

import commerce.emmerce.config.exception.GlobalErrorAttributes;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.dto.CategoryDTO;
import commerce.emmerce.service.CategoryService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser
@WebFluxTest(AdminCategoryController.class)
class AdminCategoryControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    @DisplayName("카테고리 추가 테스트")
    void createCategory() {
        // given
        CategoryDTO.CreateReq createReq = new CategoryDTO.CreateReq(1, "캐주얼/유니섹스", "10000", "-");

        // when
        when(categoryService.create(any(CategoryDTO.CreateReq.class))).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/admin/category")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(categoryService).create(any(CategoryDTO.CreateReq.class));
    }
}