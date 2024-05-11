package commerce.emmerce.service;

import commerce.emmerce.config.s3.S3FileUploader;
import commerce.emmerce.domain.Product;
import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.dto.SearchParamDTO;
import commerce.emmerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(ProductService.class)
class ProductServiceTest {
    @Autowired
    private ProductService productService;

    @MockBean
    private S3FileUploader s3FileUploader;

    @MockBean
    private ProductRepository productRepository;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setup() {
        product1 = Product.createProduct()
                .productId(1L)
                .name("바지")
                .detail("찢어진 바지")
                .originalPrice(10000)
                .discountPrice(8000)
                .discountRate(20)
                .stockQuantity(100)
                .starScore(4.5)
                .totalReviews(20)
                .titleImg("바지 이미지")
                .detailImgList(List.of("바지 상세 이미지1", "바지 상세 이미지2"))
                .brand("이머스몰")
                .enrollTime(LocalDateTime.now())
                .build();

        product2 = Product.createProduct()
                .productId(2L)
                .name("티셔츠")
                .detail("T없이 맑은 셔츠")
                .originalPrice(16000)
                .discountPrice(8000)
                .discountRate(50)
                .stockQuantity(100)
                .starScore(4.8)
                .totalReviews(20)
                .titleImg("셔츠 이미지")
                .detailImgList(List.of("셔츠 상세 이미지1", "셔츠 상세 이미지2"))
                .brand("이머스몰")
                .enrollTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("상품 추가 테스트")
    void create() {
        // given
        ProductDTO.ProductReq productReq = ProductDTO.ProductReq.builder()
                .name(product1.getName())
                .detail(product1.getDetail())
                .originalPrice(product1.getOriginalPrice())
                .discountPrice(product1.getDiscountPrice())
                .stockQuantity(product1.getStockQuantity())
                .brand(product1.getBrand())
                .build();

        // when
        when(s3FileUploader.uploadS3Image(any(), anyString())).thenReturn(Mono.just("titleImageUrl"));
        when(s3FileUploader.uploadS3ImageList(any(), anyString())).thenReturn(Mono.just(List.of("detailImageUrl1", "detailImageUrl2")));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.empty());

        StepVerifier.create(productService.create(Mono.just(productReq), null, null))
                .verifyComplete();

        // then
        verify(s3FileUploader, times(1)).uploadS3Image(any(), anyString());
        verify(s3FileUploader, times(1)).uploadS3ImageList(any(), anyString());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 상세 정보 조회 테스트")
    void detail() {
        // given
        ProductDTO.DetailResp detailResp = ProductDTO.DetailResp.builder()
                .productId(product1.getProductId())
                .name(product1.getName())
                .detail(product1.getDetail())
                .originalPrice(product1.getOriginalPrice())
                .discountPrice(product1.getDiscountPrice())
                .discountRate(product1.getDiscountRate())
                .stockQuantity(product1.getStockQuantity())
                .starScore(product1.getStarScore())
                .totalReviews(product1.getTotalReviews())
                .titleImg(product1.getTitleImg())
                .detailImgList(product1.getDetailImgList())
                .brand(product1.getBrand())
                .enrollTime(product1.getEnrollTime())
                .likeCount(10L)
                .build();

        // when
        when(productRepository.findDetailById(product1.getProductId())).thenReturn(Mono.just(detailResp));

        StepVerifier.create(productService.detail(product1.getProductId()))
                .expectNextMatches(result ->
                        result.getProductId() == detailResp.getProductId() &&
                                result.getName().equals(detailResp.getName()) &&
                                result.getDetail().equals(detailResp.getDetail()) &&
                                result.getOriginalPrice() == detailResp.getOriginalPrice() &&
                                result.getDiscountPrice() == detailResp.getDiscountPrice() &&
                                result.getDiscountRate() == detailResp.getDiscountRate() &&
                                result.getStockQuantity() == detailResp.getStockQuantity() &&
                                result.getStarScore() == detailResp.getStarScore() &&
                                result.getTotalReviews() == detailResp.getTotalReviews() &&
                                result.getTitleImg().equals(detailResp.getTitleImg()) &&
                                result.getDetailImgList().size() == detailResp.getDetailImgList().size() &&
                                result.getBrand().equals(detailResp.getBrand()) &&
                                result.getLikeCount().equals(detailResp.getLikeCount())
                ).verifyComplete();

        // then
        verify(productRepository, times(1)).findDetailById(anyLong());
    }

    @Test
    @DisplayName("상품 수정 테스트")
    void update() {
        // given
        ProductDTO.UpdateReq updateReq = ProductDTO.UpdateReq.builder()
                .name(product1.getName())
                .detail(product1.getDetail())
                .originalPrice(product1.getOriginalPrice())
                .discountPrice(product1.getDiscountPrice())
                .stockQuantity(product1.getStockQuantity())
                .build();

        // when
        when(s3FileUploader.uploadS3Image(any(), anyString())).thenReturn(Mono.just("titleImageUrl"));
        when(s3FileUploader.uploadS3ImageList(any(), anyString())).thenReturn(Mono.just(List.of("detailImageUrl1", "detailImageUrl2")));
        when(productRepository.findById(product1.getProductId())).thenReturn(Mono.just(product1));
        when(s3FileUploader.deleteS3Image(eq(product1.getTitleImg()), anyString())).thenReturn(Mono.empty());
        when(s3FileUploader.deleteS3ImageList(eq(product1.getDetailImgList()), anyString())).thenReturn(Flux.empty());
        when(productRepository.save(any(Product.class))).thenReturn(Mono.empty());

        StepVerifier.create(productService.update(product1.getProductId(), Mono.just(updateReq), null, null))
                .verifyComplete();

        // then
        verify(s3FileUploader, times(1)).uploadS3Image(any(), anyString());
        verify(s3FileUploader, times(1)).uploadS3ImageList(any(), anyString());
        verify(s3FileUploader, times(1)).deleteS3Image(any(), anyString());
        verify(s3FileUploader, times(1)).deleteS3ImageList(anyList(), anyString());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("최신 상품 목록 조회 테스트")
    void latest() {
        ProductDTO.ListResp listResp1 = ProductDTO.ListResp.builder()
                .productId(product1.getProductId())
                .name(product1.getName())
                .originalPrice(product1.getOriginalPrice())
                .discountPrice(product1.getDiscountPrice())
                .discountRate(product1.getDiscountRate())
                .starScore(product1.getStarScore())
                .totalReviews(product1.getTotalReviews())
                .titleImg(product1.getTitleImg())
                .likeCount(10L)
                .brand(product1.getBrand())
                .build();

        ProductDTO.ListResp listResp2 = ProductDTO.ListResp.builder()
                .productId(product2.getProductId())
                .name(product2.getName())
                .originalPrice(product2.getOriginalPrice())
                .discountPrice(product2.getDiscountPrice())
                .discountRate(product2.getDiscountRate())
                .starScore(product2.getStarScore())
                .totalReviews(product2.getTotalReviews())
                .titleImg(product2.getTitleImg())
                .likeCount(20L)
                .brand(product2.getBrand())
                .build();

        // when
        when(productRepository.findLatestProducts(0)).thenReturn(Flux.just(listResp1, listResp2));

        StepVerifier.create(productService.latest(0))
                .expectNext(listResp1)
                .expectNext(listResp2)
                .verifyComplete();

        // then
        verify(productRepository, times(1)).findLatestProducts(anyInt());
    }

    @Test
    @DisplayName("상품 검색 테스트")
    void search() {
        // given
        ProductDTO.ListResp listResp1 = ProductDTO.ListResp.builder()
                .productId(product1.getProductId())
                .name(product1.getName())
                .originalPrice(product1.getOriginalPrice())
                .discountPrice(product1.getDiscountPrice())
                .discountRate(product1.getDiscountRate())
                .starScore(product1.getStarScore())
                .totalReviews(product1.getTotalReviews())
                .titleImg(product1.getTitleImg())
                .likeCount(10L)
                .brand(product1.getBrand())
                .build();

        ProductDTO.ListResp listResp2 = ProductDTO.ListResp.builder()
                .productId(product2.getProductId())
                .name(product2.getName())
                .originalPrice(product2.getOriginalPrice())
                .discountPrice(product2.getDiscountPrice())
                .discountRate(product2.getDiscountRate())
                .starScore(product2.getStarScore())
                .totalReviews(product2.getTotalReviews())
                .titleImg(product2.getTitleImg())
                .likeCount(20L)
                .brand(product2.getBrand())
                .build();

        PageResponseDTO<ProductDTO.ListResp> pageResponseDTO = new PageResponseDTO<>(List.of(listResp1, listResp2), 1, 1, 2, true, true);

        SearchParamDTO searchParamDTO = SearchParamDTO.builder()
                .keyword("키워드")
                .brand("이머스몰")
                .minPrice(0)
                .maxPrice(50000)
                .sort("priceDesc")
                .page(1)
                .size(2)
                .build();

        // when
        when(productRepository.searchProductsCount(searchParamDTO)).thenReturn(Mono.just(2L));
        when(productRepository.searchProducts(searchParamDTO)).thenReturn(Flux.just(listResp1, listResp2));

        StepVerifier.create(productService.search(searchParamDTO))
                .expectNextMatches(result ->
                        result.getContent().get(0).getProductId() == pageResponseDTO.getContent().get(0).getProductId() &&
                                result.getContent().get(0).getName().equals(pageResponseDTO.getContent().get(0).getName()) &&
                                result.getContent().get(0).getOriginalPrice() == pageResponseDTO.getContent().get(0).getOriginalPrice() &&
                                result.getContent().get(0).getDiscountPrice() == pageResponseDTO.getContent().get(0).getDiscountPrice() &&
                                result.getContent().get(0).getDiscountRate() == pageResponseDTO.getContent().get(0).getDiscountRate() &&
                                result.getContent().get(0).getStarScore() == pageResponseDTO.getContent().get(0).getStarScore() &&
                                result.getContent().get(0).getTotalReviews() == pageResponseDTO.getContent().get(0).getTotalReviews() &&
                                result.getContent().get(0).getTitleImg().equals(pageResponseDTO.getContent().get(0).getTitleImg()) &&
                                result.getContent().get(0).getLikeCount().equals(pageResponseDTO.getContent().get(0).getLikeCount()) &&
                                result.getContent().get(0).getBrand().equals(pageResponseDTO.getContent().get(0).getBrand()) &&
                                result.getContent().get(1).getProductId() == pageResponseDTO.getContent().get(1).getProductId() &&
                                result.getContent().get(1).getName().equals(pageResponseDTO.getContent().get(1).getName()) &&
                                result.getContent().get(1).getOriginalPrice() == pageResponseDTO.getContent().get(1).getOriginalPrice() &&
                                result.getContent().get(1).getDiscountPrice() == pageResponseDTO.getContent().get(1).getDiscountPrice() &&
                                result.getContent().get(1).getDiscountRate() == pageResponseDTO.getContent().get(1).getDiscountRate() &&
                                result.getContent().get(1).getStarScore() == pageResponseDTO.getContent().get(1).getStarScore() &&
                                result.getContent().get(1).getTotalReviews() == pageResponseDTO.getContent().get(1).getTotalReviews() &&
                                result.getContent().get(1).getTitleImg().equals(pageResponseDTO.getContent().get(1).getTitleImg()) &&
                                result.getContent().get(1).getLikeCount().equals(pageResponseDTO.getContent().get(1).getLikeCount()) &&
                                result.getContent().get(1).getBrand().equals(pageResponseDTO.getContent().get(1).getBrand()) &&
                                result.getPageNumber() == searchParamDTO.getPage() &&
                                result.getTotalPages() == pageResponseDTO.getTotalPages() &&
                                result.getTotalElements() == pageResponseDTO.getTotalElements() &&
                                result.isFirst() == pageResponseDTO.isFirst() &&
                                result.isLast() == pageResponseDTO.isLast()
                ).verifyComplete();

        // then
        verify(productRepository, times(1)).searchProductsCount(any(SearchParamDTO.class));
        verify(productRepository, times(1)).searchProducts(any(SearchParamDTO.class));
    }

    @Test
    @DisplayName("할인률 큰 상품 목록 조회 테스트")
    void hotDeal() {
        ProductDTO.ListResp listResp1 = ProductDTO.ListResp.builder()
                .productId(product1.getProductId())
                .name(product1.getName())
                .originalPrice(product1.getOriginalPrice())
                .discountPrice(product1.getDiscountPrice())
                .discountRate(product1.getDiscountRate())
                .starScore(product1.getStarScore())
                .totalReviews(product1.getTotalReviews())
                .titleImg(product1.getTitleImg())
                .likeCount(10L)
                .brand(product1.getBrand())
                .build();

        ProductDTO.ListResp listResp2 = ProductDTO.ListResp.builder()
                .productId(product2.getProductId())
                .name(product2.getName())
                .originalPrice(product2.getOriginalPrice())
                .discountPrice(product2.getDiscountPrice())
                .discountRate(product2.getDiscountRate())
                .starScore(product2.getStarScore())
                .totalReviews(product2.getTotalReviews())
                .titleImg(product2.getTitleImg())
                .likeCount(20L)
                .brand(product2.getBrand())
                .build();

        // when
        when(productRepository.findHotDealProducts(0)).thenReturn(Flux.just(listResp1, listResp2));

        StepVerifier.create(productService.hotDeal(0))
                .expectNext(listResp1)
                .expectNext(listResp2)
                .verifyComplete();

        // then
        verify(productRepository, times(1)).findHotDealProducts(anyInt());
    }

    @Test
    @DisplayName("많이 팔린 상품 목록 조회 테스트")
    void ranking() {
        // given
        ProductDTO.ListResp listResp1 = ProductDTO.ListResp.builder()
                .productId(product1.getProductId())
                .name(product1.getName())
                .originalPrice(product1.getOriginalPrice())
                .discountPrice(product1.getDiscountPrice())
                .discountRate(product1.getDiscountRate())
                .starScore(product1.getStarScore())
                .totalReviews(product1.getTotalReviews())
                .titleImg(product1.getTitleImg())
                .likeCount(10L)
                .brand(product1.getBrand())
                .build();

        ProductDTO.ListResp listResp2 = ProductDTO.ListResp.builder()
                .productId(product2.getProductId())
                .name(product2.getName())
                .originalPrice(product2.getOriginalPrice())
                .discountPrice(product2.getDiscountPrice())
                .discountRate(product2.getDiscountRate())
                .starScore(product2.getStarScore())
                .totalReviews(product2.getTotalReviews())
                .titleImg(product2.getTitleImg())
                .likeCount(20L)
                .brand(product2.getBrand())
                .build();

        // when
        when(productRepository.findRankingProducts(0)).thenReturn(Flux.just(listResp1, listResp2));

        StepVerifier.create(productService.ranking(0))
                .expectNext(listResp1)
                .expectNext(listResp2)
                .verifyComplete();

        // then
        verify(productRepository, times(1)).findRankingProducts(anyInt());
    }
}