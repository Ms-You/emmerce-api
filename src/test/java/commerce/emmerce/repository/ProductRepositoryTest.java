package commerce.emmerce.repository;

import commerce.emmerce.domain.Product;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.dto.SearchParamDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataR2dbcTest
class ProductRepositoryTest {
    private ProductRepository productRepository;
    private DatabaseClient databaseClient;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private FetchSpec<Map<String, Object>> fetchSpec;

    private Product product;

    @BeforeEach
    void setup() {
        product = Product.createProduct()
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

        databaseClient = mock(DatabaseClient.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        fetchSpec = mock(FetchSpec.class);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);

        productRepository = new ProductRepository(databaseClient);
    }

    @Test
    void save_insert() {
        // given
        product = Product.createProduct()
                .productId(null)
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

        // when
        when(executeSpec.bind("name", product.getName())).thenReturn(executeSpec);
        when(executeSpec.bind("detail", product.getDetail())).thenReturn(executeSpec);
        when(executeSpec.bind("originalPrice", product.getOriginalPrice())).thenReturn(executeSpec);
        when(executeSpec.bind("discountPrice", product.getDiscountPrice())).thenReturn(executeSpec);
        when(executeSpec.bind("discountRate", product.getDiscountRate())).thenReturn(executeSpec);
        when(executeSpec.bind("stockQuantity", product.getStockQuantity())).thenReturn(executeSpec);
        when(executeSpec.bind("starScore", product.getStarScore())).thenReturn(executeSpec);
        when(executeSpec.bind("totalReviews", product.getTotalReviews())).thenReturn(executeSpec);
        when(executeSpec.bind("titleImg", product.getTitleImg())).thenReturn(executeSpec);
        when(executeSpec.bind("detailImgList", product.getDetailImgList().toArray())).thenReturn(executeSpec);
        when(executeSpec.bind("brand", product.getBrand())).thenReturn(executeSpec);
        when(executeSpec.bind("enrollTime", product.getEnrollTime())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(productRepository.save(product))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(12)).bind(anyString(), any());
    }

    @Test
    void save_update() {
        // given
        // when
        when(executeSpec.bind("name", product.getName())).thenReturn(executeSpec);
        when(executeSpec.bind("detail", product.getDetail())).thenReturn(executeSpec);
        when(executeSpec.bind("originalPrice", product.getOriginalPrice())).thenReturn(executeSpec);
        when(executeSpec.bind("discountPrice", product.getDiscountPrice())).thenReturn(executeSpec);
        when(executeSpec.bind("discountRate", product.getDiscountRate())).thenReturn(executeSpec);
        when(executeSpec.bind("stockQuantity", product.getStockQuantity())).thenReturn(executeSpec);
        when(executeSpec.bind("starScore", product.getStarScore())).thenReturn(executeSpec);
        when(executeSpec.bind("totalReviews", product.getTotalReviews())).thenReturn(executeSpec);
        when(executeSpec.bind("titleImg", product.getTitleImg())).thenReturn(executeSpec);
        when(executeSpec.bind("detailImgList", product.getDetailImgList().toArray())).thenReturn(executeSpec);
        when(executeSpec.bind("brand", product.getBrand())).thenReturn(executeSpec);
        when(executeSpec.bind("enrollTime", product.getEnrollTime())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", product.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(productRepository.save(product))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(13)).bind(anyString(), any());
    }

    @Test
    void findById() {
        // given
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("product_id", product.getProductId());
        dataMap.put("name", product.getName());
        dataMap.put("detail", product.getDetail());
        dataMap.put("original_price", product.getOriginalPrice());
        dataMap.put("discount_price", product.getDiscountPrice());
        dataMap.put("discount_rate", product.getDiscountRate());
        dataMap.put("stock_quantity", product.getStockQuantity());
        dataMap.put("star_score", product.getStarScore());
        dataMap.put("total_reviews", product.getTotalReviews());
        dataMap.put("title_img", product.getTitleImg());
        dataMap.put("detail_img_list", product.getDetailImgList().toArray(new String[product.getDetailImgList().size()]));
        dataMap.put("brand", product.getBrand());
        dataMap.put("enroll_time", product.getEnrollTime());

        // when
        when(executeSpec.bind("productId", product.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(productRepository.findById(product.getProductId()))
                .expectNextMatches(result ->
                        result.getProductId() == product.getProductId() &&
                                result.getName().equals(product.getName()) &&
                                result.getDetail().equals(product.getDetail()) &&
                                result.getOriginalPrice() == product.getOriginalPrice() &&
                                result.getDiscountPrice() == product.getDiscountPrice() &&
                                result.getDiscountRate() == product.getDiscountRate() &&
                                result.getStockQuantity() == product.getStockQuantity() &&
                                result.getStarScore() == product.getStarScore() &&
                                result.getTotalReviews() == product.getTotalReviews() &&
                                result.getTitleImg().equals(product.getTitleImg()) &&
                                result.getDetailImgList().size() == product.getDetailImgList().size() &&
                                result.getBrand().equals(product.getBrand()) &&
                                result.getEnrollTime().isEqual(product.getEnrollTime())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void findAll() {
        // given
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("product_id", product.getProductId());
        dataMap.put("name", product.getName());
        dataMap.put("detail", product.getDetail());
        dataMap.put("original_price", product.getOriginalPrice());
        dataMap.put("discount_price", product.getDiscountPrice());
        dataMap.put("discount_rate", product.getDiscountRate());
        dataMap.put("stock_quantity", product.getStockQuantity());
        dataMap.put("star_score", product.getStarScore());
        dataMap.put("total_reviews", product.getTotalReviews());
        dataMap.put("title_img", product.getTitleImg());
        dataMap.put("detail_img_list", product.getDetailImgList().toArray(new String[product.getDetailImgList().size()]));
        dataMap.put("brand", product.getBrand());
        dataMap.put("enroll_time", product.getEnrollTime());

        // when
        when(fetchSpec.all()).thenReturn(Flux.just(dataMap));

        StepVerifier.create(productRepository.findAll())
                .expectNextMatches(result ->
                        result.getProductId() == product.getProductId() &&
                                result.getName().equals(product.getName()) &&
                                result.getDetail().equals(product.getDetail()) &&
                                result.getOriginalPrice() == product.getOriginalPrice() &&
                                result.getDiscountPrice() == product.getDiscountPrice() &&
                                result.getDiscountRate() == product.getDiscountRate() &&
                                result.getStockQuantity() == product.getStockQuantity() &&
                                result.getStarScore() == product.getStarScore() &&
                                result.getTotalReviews() == product.getTotalReviews() &&
                                result.getTitleImg().equals(product.getTitleImg()) &&
                                result.getDetailImgList().size() == product.getDetailImgList().size() &&
                                result.getBrand().equals(product.getBrand()) &&
                                result.getEnrollTime().isEqual(product.getEnrollTime())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
    }

    @Test
    void findDetailById() {
        // given
        ProductDTO.DetailResp detailResp = ProductDTO.DetailResp.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .detail(product.getDetail())
                .originalPrice(product.getOriginalPrice())
                .discountPrice(product.getDiscountPrice())
                .discountRate(product.getDiscountRate())
                .stockQuantity(product.getStockQuantity())
                .starScore(product.getStarScore())
                .totalReviews(product.getTotalReviews())
                .titleImg(product.getTitleImg())
                .detailImgList(product.getDetailImgList())
                .brand(product.getBrand())
                .enrollTime(product.getEnrollTime())
                .likeCount(10L)
                .build();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("product_id", product.getProductId());
        dataMap.put("name", product.getName());
        dataMap.put("detail", product.getDetail());
        dataMap.put("original_price", product.getOriginalPrice());
        dataMap.put("discount_price", product.getDiscountPrice());
        dataMap.put("discount_rate", product.getDiscountRate());
        dataMap.put("stock_quantity", product.getStockQuantity());
        dataMap.put("star_score", product.getStarScore());
        dataMap.put("total_reviews", product.getTotalReviews());
        dataMap.put("title_img", product.getTitleImg());
        dataMap.put("detail_img_list", product.getDetailImgList().toArray(new String[product.getDetailImgList().size()]));
        dataMap.put("brand", product.getBrand());
        dataMap.put("enroll_time", product.getEnrollTime());
        dataMap.put("like_count", 10L);

        // when
        when(executeSpec.bind("productId", product.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(productRepository.findDetailById(product.getProductId()))
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
                                result.getEnrollTime().isEqual(detailResp.getEnrollTime()) &&
                                result.getLikeCount() == detailResp.getLikeCount()
                ).verifyComplete();
        
        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void findLatestProducts() {
        // given
        ProductDTO.ListResp listResp = ProductDTO.ListResp.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .originalPrice(product.getOriginalPrice())
                .discountPrice(product.getDiscountPrice())
                .discountRate(product.getDiscountRate())
                .starScore(product.getStarScore())
                .totalReviews(product.getTotalReviews())
                .titleImg(product.getTitleImg())
                .likeCount(10L)
                .brand(product.getBrand())
                .build();

        Map<String, Object> dataMap = Map.of(
                "product_id", product.getProductId(),
                "name", product.getName(),
                "original_price", product.getOriginalPrice(),
                "discount_price", product.getDiscountPrice(),
                "discount_rate", product.getDiscountRate(),
                "star_score", product.getStarScore(),
                "total_reviews", product.getTotalReviews(),
                "title_img", product.getTitleImg(),
                "like_count", 10L,
                "brand", product.getBrand()
        );

        // when
        when(executeSpec.bind("size", 1)).thenReturn(executeSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(dataMap));

        StepVerifier.create(productRepository.findLatestProducts(1))
                .expectNextMatches(result ->
                        result.getProductId() == listResp.getProductId() &&
                                result.getName().equals(listResp.getName()) &&
                                result.getOriginalPrice() == listResp.getOriginalPrice() &&
                                result.getDiscountPrice() == listResp.getDiscountPrice() &&
                                result.getDiscountRate() == listResp.getDiscountRate() &&
                                result.getStarScore() == listResp.getStarScore() &&
                                result.getTotalReviews() == listResp.getTotalReviews() &&
                                result.getTitleImg().equals(listResp.getTitleImg()) &&
                                result.getLikeCount() == listResp.getLikeCount() &&
                                result.getBrand().equals(listResp.getBrand())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void searchProducts() {
        // given
        ProductDTO.ListResp listResp = ProductDTO.ListResp.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .originalPrice(product.getOriginalPrice())
                .discountPrice(product.getDiscountPrice())
                .discountRate(product.getDiscountRate())
                .starScore(product.getStarScore())
                .totalReviews(product.getTotalReviews())
                .titleImg(product.getTitleImg())
                .likeCount(10L)
                .brand(product.getBrand())
                .build();

        Map<String, Object> dataMap = Map.of(
                "product_id", product.getProductId(),
                "name", product.getName(),
                "original_price", product.getOriginalPrice(),
                "discount_price", product.getDiscountPrice(),
                "discount_rate", product.getDiscountRate(),
                "star_score", product.getStarScore(),
                "total_reviews", product.getTotalReviews(),
                "title_img", product.getTitleImg(),
                "like_count", 10L,
                "brand", product.getBrand()
        );

        SearchParamDTO searchParamDTO = SearchParamDTO.builder()
                .keyword("키워드")
                .brand("이머스몰")
                .minPrice(0)
                .maxPrice(50000)
                .sort("priceDesc")
                .page(1)
                .size(1)
                .build();

        // when
        when(executeSpec.bind("keyword", searchParamDTO.getKeyword())).thenReturn(executeSpec);
        when(executeSpec.bind("brand", searchParamDTO.getBrand())).thenReturn(executeSpec);
        when(executeSpec.bind("minPrice", searchParamDTO.getMinPrice())).thenReturn(executeSpec);
        when(executeSpec.bind("maxPrice", searchParamDTO.getMaxPrice())).thenReturn(executeSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(dataMap));

        StepVerifier.create(productRepository.searchProducts(searchParamDTO))
                .expectNextMatches(result ->
                        result.getProductId() == listResp.getProductId() &&
                                result.getName().equals(listResp.getName()) &&
                                result.getOriginalPrice() == listResp.getOriginalPrice() &&
                                result.getDiscountPrice() == listResp.getDiscountPrice() &&
                                result.getDiscountRate() == listResp.getDiscountRate() &&
                                result.getStarScore() == listResp.getStarScore() &&
                                result.getTotalReviews() == listResp.getTotalReviews() &&
                                result.getTitleImg().equals(listResp.getTitleImg()) &&
                                result.getLikeCount() == listResp.getLikeCount() &&
                                result.getBrand().equals(listResp.getBrand())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(4)).bind(anyString(), any());
    }

    @Test
    void searchProductsCount() {
        // given
        SearchParamDTO searchParamDTO = SearchParamDTO.builder()
                .keyword("키워드")
                .brand("이머스몰")
                .minPrice(0)
                .maxPrice(50000)
                .sort("priceDesc")
                .page(1)
                .size(1)
                .build();

        // when
        when(executeSpec.bind("keyword", searchParamDTO.getKeyword())).thenReturn(executeSpec);
        when(executeSpec.bind("brand", searchParamDTO.getBrand())).thenReturn(executeSpec);
        when(executeSpec.bind("minPrice", searchParamDTO.getMinPrice())).thenReturn(executeSpec);
        when(executeSpec.bind("maxPrice", searchParamDTO.getMaxPrice())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(Map.of("count", 1L)));

        StepVerifier.create(productRepository.searchProductsCount(searchParamDTO))
                .expectNext(1L)
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(4)).bind(anyString(), any());
    }

    @Test
    void findHotDealProducts() {
        // given
        ProductDTO.ListResp listResp = ProductDTO.ListResp.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .originalPrice(product.getOriginalPrice())
                .discountPrice(product.getDiscountPrice())
                .discountRate(product.getDiscountRate())
                .starScore(product.getStarScore())
                .totalReviews(product.getTotalReviews())
                .titleImg(product.getTitleImg())
                .likeCount(10L)
                .brand(product.getBrand())
                .build();

        Map<String, Object> dataMap = Map.of(
                "product_id", product.getProductId(),
                "name", product.getName(),
                "original_price", product.getOriginalPrice(),
                "discount_price", product.getDiscountPrice(),
                "discount_rate", product.getDiscountRate(),
                "star_score", product.getStarScore(),
                "total_reviews", product.getTotalReviews(),
                "title_img", product.getTitleImg(),
                "like_count", 10L,
                "brand", product.getBrand()
        );

        // when
        when(executeSpec.bind("size", 1)).thenReturn(executeSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(dataMap));

        StepVerifier.create(productRepository.findHotDealProducts(1))
                .expectNextMatches(result ->
                        result.getProductId() == listResp.getProductId() &&
                                result.getName().equals(listResp.getName()) &&
                                result.getOriginalPrice() == listResp.getOriginalPrice() &&
                                result.getDiscountPrice() == listResp.getDiscountPrice() &&
                                result.getDiscountRate() == listResp.getDiscountRate() &&
                                result.getStarScore() == listResp.getStarScore() &&
                                result.getTotalReviews() == listResp.getTotalReviews() &&
                                result.getTitleImg().equals(listResp.getTitleImg()) &&
                                result.getLikeCount() == listResp.getLikeCount() &&
                                result.getBrand().equals(listResp.getBrand())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void findRankingProducts() {
        // given
        ProductDTO.ListResp listResp = ProductDTO.ListResp.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .originalPrice(product.getOriginalPrice())
                .discountPrice(product.getDiscountPrice())
                .discountRate(product.getDiscountRate())
                .starScore(product.getStarScore())
                .totalReviews(product.getTotalReviews())
                .titleImg(product.getTitleImg())
                .likeCount(10L)
                .brand(product.getBrand())
                .build();

        Map<String, Object> dataMap = Map.of(
                "product_id", product.getProductId(),
                "name", product.getName(),
                "original_price", product.getOriginalPrice(),
                "discount_price", product.getDiscountPrice(),
                "discount_rate", product.getDiscountRate(),
                "star_score", product.getStarScore(),
                "total_reviews", product.getTotalReviews(),
                "title_img", product.getTitleImg(),
                "like_count", 10L,
                "brand", product.getBrand()
        );

        // when
        when(executeSpec.bind("size", 1)).thenReturn(executeSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(dataMap));

        StepVerifier.create(productRepository.findRankingProducts(1))
                .expectNextMatches(result ->
                        result.getProductId() == listResp.getProductId() &&
                                result.getName().equals(listResp.getName()) &&
                                result.getOriginalPrice() == listResp.getOriginalPrice() &&
                                result.getDiscountPrice() == listResp.getDiscountPrice() &&
                                result.getDiscountRate() == listResp.getDiscountRate() &&
                                result.getStarScore() == listResp.getStarScore() &&
                                result.getTotalReviews() == listResp.getTotalReviews() &&
                                result.getTitleImg().equals(listResp.getTitleImg()) &&
                                result.getLikeCount() == listResp.getLikeCount() &&
                                result.getBrand().equals(listResp.getBrand())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }
}