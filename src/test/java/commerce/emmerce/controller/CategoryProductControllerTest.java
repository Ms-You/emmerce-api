package commerce.emmerce.controller;

import commerce.emmerce.config.exception.GlobalErrorAttributes;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.dto.CategoryProductDTO;
import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.SearchParamDTO;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WithMockUser
@WebFluxTest(CategoryProductController.class)
class CategoryProductControllerTest {

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
    @DisplayName("카테고리에 속한 상품 목록 조회 테스트")
    void findProductsByCategory() {
        // given
        CategoryProductDTO.ListResp productResp1 = CategoryProductDTO.ListResp.builder()
                .productId(1L)
                .name("샴푸")
                .originalPrice(10000)
                .discountPrice(8000)
                .discountRate(20)
                .starScore(4.5)
                .titleImg("샴푸 이미지")
                .likeCount(20L)
                .brand("에비앙")
                .build();

        CategoryProductDTO.ListResp productResp2 = CategoryProductDTO.ListResp.builder()
                .productId(2L)
                .name("린스")
                .originalPrice(8000)
                .discountPrice(6000)
                .discountRate(25)
                .starScore(4.8)
                .titleImg("린스 이미지")
                .likeCount(10L)
                .brand("삼다수")
                .build();

        PageResponseDTO<CategoryProductDTO.ListResp> pageResponseDTO = new PageResponseDTO<>(List.of(productResp1, productResp2), 1, 2, 2);

        // when
        when(categoryProductService.findCategoryProductList(anyLong(), any(SearchParamDTO.class))).thenReturn(Mono.just(pageResponseDTO));

        webTestClient
                .get()
                .uri("/category/{categoryId}/product/list", 1L)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.[0].productId").isEqualTo(pageResponseDTO.getContent().get(0).getProductId())
                .jsonPath("$.content.[0].name").isEqualTo(pageResponseDTO.getContent().get(0).getName())
                .jsonPath("$.content.[0].originalPrice").isEqualTo(pageResponseDTO.getContent().get(0).getOriginalPrice())
                .jsonPath("$.content.[0].discountPrice").isEqualTo(pageResponseDTO.getContent().get(0).getDiscountPrice())
                .jsonPath("$.content.[0].discountRate").isEqualTo(pageResponseDTO.getContent().get(0).getDiscountRate())
                .jsonPath("$.content.[0].starScore").isEqualTo(pageResponseDTO.getContent().get(0).getStarScore())
                .jsonPath("$.content.[0].titleImg").isEqualTo(pageResponseDTO.getContent().get(0).getTitleImg())
                .jsonPath("$.content.[0].likeCount").isEqualTo(pageResponseDTO.getContent().get(0).getLikeCount())
                .jsonPath("$.content.[0].brand").isEqualTo(pageResponseDTO.getContent().get(0).getBrand())
                .jsonPath("$.content.[1].productId").isEqualTo(pageResponseDTO.getContent().get(1).getProductId())
                .jsonPath("$.content.[1].name").isEqualTo(pageResponseDTO.getContent().get(1).getName())
                .jsonPath("$.content.[1].originalPrice").isEqualTo(pageResponseDTO.getContent().get(1).getOriginalPrice())
                .jsonPath("$.content.[1].discountPrice").isEqualTo(pageResponseDTO.getContent().get(1).getDiscountPrice())
                .jsonPath("$.content.[1].discountRate").isEqualTo(pageResponseDTO.getContent().get(1).getDiscountRate())
                .jsonPath("$.content.[1].starScore").isEqualTo(pageResponseDTO.getContent().get(1).getStarScore())
                .jsonPath("$.content.[1].titleImg").isEqualTo(pageResponseDTO.getContent().get(1).getTitleImg())
                .jsonPath("$.content.[1].likeCount").isEqualTo(pageResponseDTO.getContent().get(1).getLikeCount())
                .jsonPath("$.content.[1].brand").isEqualTo(pageResponseDTO.getContent().get(1).getBrand())
                .jsonPath("$.pageNumber").isEqualTo(pageResponseDTO.getPageNumber())
                .jsonPath("$.totalPages").isEqualTo(pageResponseDTO.getTotalPages())
                .jsonPath("$.totalElements").isEqualTo(pageResponseDTO.getTotalElements())
                .jsonPath("$.first").isEqualTo(pageResponseDTO.isFirst())
                .jsonPath("$.last").isEqualTo(pageResponseDTO.isLast());

        // then
        verify(categoryProductService).findCategoryProductList(anyLong(), any(SearchParamDTO.class));
    }
}