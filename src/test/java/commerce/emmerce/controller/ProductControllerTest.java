package commerce.emmerce.controller;

import commerce.emmerce.config.exception.GlobalErrorAttributes;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.dto.SearchParamDTO;
import commerce.emmerce.service.ProductService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WithMockUser
@WebFluxTest(ProductController.class)
class ProductControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    @DisplayName("상품 상세 정보 조회 테스트")
    void productDetail() {
        // given
        Long productId = 1L;

        ProductDTO.DetailResp detailResp = ProductDTO.DetailResp.builder()
                .productId(productId)
                .name("샴푸")
                .detail("강력 추천! 머리가 자라나는 샴푸")
                .originalPrice(10000)
                .discountPrice(8000)
                .discountRate(20)
                .stockQuantity(100)
                .starScore(4.5)
                .totalReviews(20)
                .titleImg("샴푸 이미지")
                .detailImgList(List.of("샴푸 상세 이미지1", "샴푸 상세 이미지2"))
                .brand("에비앙")
                .enrollTime(LocalDateTime.now())
                .likeCount(20L)
                .build();

        // when
        when(productService.detail(anyLong())).thenReturn(Mono.just(detailResp));

        webTestClient
                .get()
                .uri("/product/{productId}", productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.productId").isEqualTo(detailResp.getProductId())
                .jsonPath("$.name").isEqualTo(detailResp.getName())
                .jsonPath("$.detail").isEqualTo(detailResp.getDetail())
                .jsonPath("$.originalPrice").isEqualTo(detailResp.getOriginalPrice())
                .jsonPath("$.discountPrice").isEqualTo(detailResp.getDiscountPrice())
                .jsonPath("$.discountRate").isEqualTo(detailResp.getDiscountRate())
                .jsonPath("$.stockQuantity").isEqualTo(detailResp.getStockQuantity())
                .jsonPath("$.starScore").isEqualTo(detailResp.getStarScore())
                .jsonPath("$.totalReviews").isEqualTo(detailResp.getTotalReviews())
                .jsonPath("$.titleImg").isEqualTo(detailResp.getTitleImg())
                .jsonPath("$.detailImgList").isArray()
                .jsonPath("$.brand").isEqualTo(detailResp.getBrand())
                .jsonPath("$.enrollTime").exists()
                .jsonPath("$.likeCount").isEqualTo(detailResp.getLikeCount());

        // then
        verify(productService).detail(anyLong());
    }

    @Test
    @DisplayName("최신 상품 목록 조회 테스트")
    void latestProducts() {
        // given
        ProductDTO.ListResp listResp1 = ProductDTO.ListResp.builder()
                .productId(1L)
                .name("샴푸")
                .originalPrice(10000)
                .discountPrice(8000)
                .discountRate(20)
                .starScore(4.5)
                .totalReviews(20)
                .titleImg("샴푸 이미지")
                .likeCount(20L)
                .brand("에비앙")
                .build();

        ProductDTO.ListResp listResp2 = ProductDTO.ListResp.builder()
                .productId(2L)
                .name("린스")
                .originalPrice(8000)
                .discountPrice(6000)
                .discountRate(25)
                .starScore(4.8)
                .totalReviews(10)
                .titleImg("린스 이미지")
                .likeCount(10L)
                .brand("삼다수")
                .build();

        // when
        when(productService.latest(anyInt())).thenReturn(Flux.just(listResp1, listResp2));

        webTestClient
                .get()
                .uri("/product/latest")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].productId").isEqualTo(listResp1.getProductId())
                .jsonPath("$[0].name").isEqualTo(listResp1.getName())
                .jsonPath("$[0].originalPrice").isEqualTo(listResp1.getOriginalPrice())
                .jsonPath("$[0].discountPrice").isEqualTo(listResp1.getDiscountPrice())
                .jsonPath("$[0].discountRate").isEqualTo(listResp1.getDiscountRate())
                .jsonPath("$[0].starScore").isEqualTo(listResp1.getStarScore())
                .jsonPath("$[0].totalReviews").isEqualTo(listResp1.getTotalReviews())
                .jsonPath("$[0].titleImg").isEqualTo(listResp1.getTitleImg())
                .jsonPath("$[0].likeCount").isEqualTo(listResp1.getLikeCount())
                .jsonPath("$[0].brand").isEqualTo(listResp1.getBrand())
                .jsonPath("$[1].productId").isEqualTo(listResp2.getProductId())
                .jsonPath("$[1].name").isEqualTo(listResp2.getName())
                .jsonPath("$[1].originalPrice").isEqualTo(listResp2.getOriginalPrice())
                .jsonPath("$[1].discountPrice").isEqualTo(listResp2.getDiscountPrice())
                .jsonPath("$[1].discountRate").isEqualTo(listResp2.getDiscountRate())
                .jsonPath("$[1].starScore").isEqualTo(listResp2.getStarScore())
                .jsonPath("$[1].totalReviews").isEqualTo(listResp2.getTotalReviews())
                .jsonPath("$[1].titleImg").isEqualTo(listResp2.getTitleImg())
                .jsonPath("$[1].likeCount").isEqualTo(listResp2.getLikeCount())
                .jsonPath("$[1].brand").isEqualTo(listResp2.getBrand());

        // then
        verify(productService).latest(anyInt());
    }

    @Test
    @DisplayName("상품 검색 테스트")
    void searchProducts() {
        // given
        ProductDTO.ListResp listResp1 = ProductDTO.ListResp.builder()
                .productId(1L)
                .name("샴푸")
                .originalPrice(10000)
                .discountPrice(8000)
                .discountRate(20)
                .starScore(4.5)
                .totalReviews(20)
                .titleImg("샴푸 이미지")
                .likeCount(20L)
                .brand("에비앙")
                .build();

        ProductDTO.ListResp listResp2 = ProductDTO.ListResp.builder()
                .productId(2L)
                .name("린스")
                .originalPrice(8000)
                .discountPrice(6000)
                .discountRate(25)
                .starScore(4.8)
                .totalReviews(10)
                .titleImg("린스 이미지")
                .likeCount(10L)
                .brand("삼다수")
                .build();

        PageResponseDTO<ProductDTO.ListResp> pageResponseDTO = new PageResponseDTO<>(List.of(listResp1, listResp2), 1, 2, 2);

        // when
        when(productService.search(any(SearchParamDTO.class))).thenReturn(Mono.just(pageResponseDTO));

        webTestClient
                .get()
                .uri("/product/search")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.[0].productId").isEqualTo(listResp1.getProductId())
                .jsonPath("$.content.[0].name").isEqualTo(listResp1.getName())
                .jsonPath("$.content.[0].originalPrice").isEqualTo(listResp1.getOriginalPrice())
                .jsonPath("$.content.[0].discountPrice").isEqualTo(listResp1.getDiscountPrice())
                .jsonPath("$.content.[0].discountRate").isEqualTo(listResp1.getDiscountRate())
                .jsonPath("$.content.[0].starScore").isEqualTo(listResp1.getStarScore())
                .jsonPath("$.content.[0].totalReviews").isEqualTo(listResp1.getTotalReviews())
                .jsonPath("$.content.[0].titleImg").isEqualTo(listResp1.getTitleImg())
                .jsonPath("$.content.[0].likeCount").isEqualTo(listResp1.getLikeCount())
                .jsonPath("$.content.[0].brand").isEqualTo(listResp1.getBrand())
                .jsonPath("$.content.[1].productId").isEqualTo(listResp2.getProductId())
                .jsonPath("$.content.[1].name").isEqualTo(listResp2.getName())
                .jsonPath("$.content.[1].originalPrice").isEqualTo(listResp2.getOriginalPrice())
                .jsonPath("$.content.[1].discountPrice").isEqualTo(listResp2.getDiscountPrice())
                .jsonPath("$.content.[1].discountRate").isEqualTo(listResp2.getDiscountRate())
                .jsonPath("$.content.[1].starScore").isEqualTo(listResp2.getStarScore())
                .jsonPath("$.content.[1].totalReviews").isEqualTo(listResp2.getTotalReviews())
                .jsonPath("$.content.[1].titleImg").isEqualTo(listResp2.getTitleImg())
                .jsonPath("$.content.[1].likeCount").isEqualTo(listResp2.getLikeCount())
                .jsonPath("$.content.[1].brand").isEqualTo(listResp2.getBrand())
                .jsonPath("$.pageNumber").isEqualTo(pageResponseDTO.getPageNumber())
                .jsonPath("$.totalPages").isEqualTo(pageResponseDTO.getTotalPages())
                .jsonPath("$.totalElements").isEqualTo(pageResponseDTO.getTotalElements())
                .jsonPath("$.first").isEqualTo(pageResponseDTO.isFirst())
                .jsonPath("$.last").isEqualTo(pageResponseDTO.isLast());

        // then
        verify(productService).search(any(SearchParamDTO.class));
    }

    @Test
    @DisplayName("할인률 큰 상품 목록 조회 테스트")
    void hotDeal() {
        // given
        ProductDTO.ListResp listResp1 = ProductDTO.ListResp.builder()
                .productId(1L)
                .name("샴푸")
                .originalPrice(10000)
                .discountPrice(8000)
                .discountRate(20)
                .starScore(4.5)
                .totalReviews(20)
                .titleImg("샴푸 이미지")
                .likeCount(20L)
                .brand("에비앙")
                .build();

        ProductDTO.ListResp listResp2 = ProductDTO.ListResp.builder()
                .productId(2L)
                .name("린스")
                .originalPrice(8000)
                .discountPrice(6000)
                .discountRate(25)
                .starScore(4.8)
                .totalReviews(10)
                .titleImg("린스 이미지")
                .likeCount(10L)
                .brand("삼다수")
                .build();

        // when
        when(productService.hotDeal(anyInt())).thenReturn(Flux.just(listResp1, listResp2));

        webTestClient
                .get()
                .uri("/product/hot-deal")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].productId").isEqualTo(listResp1.getProductId())
                .jsonPath("$[0].name").isEqualTo(listResp1.getName())
                .jsonPath("$[0].originalPrice").isEqualTo(listResp1.getOriginalPrice())
                .jsonPath("$[0].discountPrice").isEqualTo(listResp1.getDiscountPrice())
                .jsonPath("$[0].discountRate").isEqualTo(listResp1.getDiscountRate())
                .jsonPath("$[0].starScore").isEqualTo(listResp1.getStarScore())
                .jsonPath("$[0].totalReviews").isEqualTo(listResp1.getTotalReviews())
                .jsonPath("$[0].titleImg").isEqualTo(listResp1.getTitleImg())
                .jsonPath("$[0].likeCount").isEqualTo(listResp1.getLikeCount())
                .jsonPath("$[0].brand").isEqualTo(listResp1.getBrand())
                .jsonPath("$[1].productId").isEqualTo(listResp2.getProductId())
                .jsonPath("$[1].name").isEqualTo(listResp2.getName())
                .jsonPath("$[1].originalPrice").isEqualTo(listResp2.getOriginalPrice())
                .jsonPath("$[1].discountPrice").isEqualTo(listResp2.getDiscountPrice())
                .jsonPath("$[1].discountRate").isEqualTo(listResp2.getDiscountRate())
                .jsonPath("$[1].starScore").isEqualTo(listResp2.getStarScore())
                .jsonPath("$[1].totalReviews").isEqualTo(listResp2.getTotalReviews())
                .jsonPath("$[1].titleImg").isEqualTo(listResp2.getTitleImg())
                .jsonPath("$[1].likeCount").isEqualTo(listResp2.getLikeCount())
                .jsonPath("$[1].brand").isEqualTo(listResp2.getBrand());

        // then
        verify(productService).hotDeal(anyInt());
    }

    @Test
    @DisplayName("많이 팔린 상품 목록 조회 테스트")
    void ranking() {
        // given
        ProductDTO.ListResp listResp1 = ProductDTO.ListResp.builder()
                .productId(1L)
                .name("샴푸")
                .originalPrice(10000)
                .discountPrice(8000)
                .discountRate(20)
                .starScore(4.5)
                .totalReviews(20)
                .titleImg("샴푸 이미지")
                .likeCount(20L)
                .brand("에비앙")
                .build();

        ProductDTO.ListResp listResp2 = ProductDTO.ListResp.builder()
                .productId(2L)
                .name("린스")
                .originalPrice(8000)
                .discountPrice(6000)
                .discountRate(25)
                .starScore(4.8)
                .totalReviews(10)
                .titleImg("린스 이미지")
                .likeCount(10L)
                .brand("삼다수")
                .build();

        // when
        when(productService.ranking(anyInt())).thenReturn(Flux.just(listResp1, listResp2));

        webTestClient
                .get()
                .uri("/product/ranking")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].productId").isEqualTo(listResp1.getProductId())
                .jsonPath("$[0].name").isEqualTo(listResp1.getName())
                .jsonPath("$[0].originalPrice").isEqualTo(listResp1.getOriginalPrice())
                .jsonPath("$[0].discountPrice").isEqualTo(listResp1.getDiscountPrice())
                .jsonPath("$[0].discountRate").isEqualTo(listResp1.getDiscountRate())
                .jsonPath("$[0].starScore").isEqualTo(listResp1.getStarScore())
                .jsonPath("$[0].totalReviews").isEqualTo(listResp1.getTotalReviews())
                .jsonPath("$[0].titleImg").isEqualTo(listResp1.getTitleImg())
                .jsonPath("$[0].likeCount").isEqualTo(listResp1.getLikeCount())
                .jsonPath("$[0].brand").isEqualTo(listResp1.getBrand())
                .jsonPath("$[1].productId").isEqualTo(listResp2.getProductId())
                .jsonPath("$[1].name").isEqualTo(listResp2.getName())
                .jsonPath("$[1].originalPrice").isEqualTo(listResp2.getOriginalPrice())
                .jsonPath("$[1].discountPrice").isEqualTo(listResp2.getDiscountPrice())
                .jsonPath("$[1].discountRate").isEqualTo(listResp2.getDiscountRate())
                .jsonPath("$[1].starScore").isEqualTo(listResp2.getStarScore())
                .jsonPath("$[1].totalReviews").isEqualTo(listResp2.getTotalReviews())
                .jsonPath("$[1].titleImg").isEqualTo(listResp2.getTitleImg())
                .jsonPath("$[1].likeCount").isEqualTo(listResp2.getLikeCount())
                .jsonPath("$[1].brand").isEqualTo(listResp2.getBrand());

        // then
        verify(productService).ranking(anyInt());
    }
}