package commerce.emmerce.controller;

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
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WithMockUser
@WebFluxTest(CategoryController.class)
class CategoryControllerTest {

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
    @DisplayName("카테고리 목록 조회 테스트")
    void categories() {
        // given
        CategoryDTO.CategoryResp categoryResp1 = CategoryDTO.CategoryResp.builder()
                .categoryId(1L)
                .tier(1)
                .name("캐주얼/유니섹스")
                .code("10000")
                .parentCode("-")
                .build();

        CategoryDTO.CategoryResp categoryResp2 = CategoryDTO.CategoryResp.builder()
                .categoryId(2L)
                .tier(2)
                .name("티셔츠")
                .code("10100")
                .parentCode("10000")
                .build();

        // when
        when(categoryService.list()).thenReturn(Flux.just(categoryResp1, categoryResp2));

        webTestClient
                .get()
                .uri("/category/list")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].categoryId").isEqualTo(categoryResp1.getCategoryId())
                .jsonPath("$[0].tier").isEqualTo(categoryResp1.getTier())
                .jsonPath("$[0].name").isEqualTo(categoryResp1.getName())
                .jsonPath("$[0].code").isEqualTo(categoryResp1.getCode())
                .jsonPath("$[0].parentCode").isEqualTo(categoryResp1.getParentCode())
                .jsonPath("$[1].categoryId").isEqualTo(categoryResp2.getCategoryId())
                .jsonPath("$[1].tier").isEqualTo(categoryResp2.getTier())
                .jsonPath("$[1].name").isEqualTo(categoryResp2.getName())
                .jsonPath("$[1].code").isEqualTo(categoryResp2.getCode())
                .jsonPath("$[1].parentCode").isEqualTo(categoryResp2.getParentCode());

        // then
        verify(categoryService).list();
    }

    @Test
    @DisplayName("상품이 속한 카테고리 조회 테스트")
    void getCategoriesInfo() {
        // given
        Long productId = 1L;
        CategoryDTO.InfoResp infoResp1 = new CategoryDTO.InfoResp(1L, 1, "캐주얼/유니섹스");
        CategoryDTO.InfoResp infoResp2 = new CategoryDTO.InfoResp(2L, 2, "티셔츠");

        // when
        when(categoryService.categoriesByProduct(anyLong())).thenReturn(Flux.just(infoResp1, infoResp2));

        webTestClient
                .get()
                .uri("/product/{productId}/categories", productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].categoryId").isEqualTo(infoResp1.getCategoryId())
                .jsonPath("$[0].tier").isEqualTo(infoResp1.getTier())
                .jsonPath("$[0].name").isEqualTo(infoResp1.getName())
                .jsonPath("$[1].categoryId").isEqualTo(infoResp2.getCategoryId())
                .jsonPath("$[1].tier").isEqualTo(infoResp2.getTier())
                .jsonPath("$[1].name").isEqualTo(infoResp2.getName());

        // then
        verify(categoryService).categoriesByProduct(anyLong());
    }
}