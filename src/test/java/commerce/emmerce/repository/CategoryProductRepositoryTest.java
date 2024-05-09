package commerce.emmerce.repository;

import commerce.emmerce.domain.CategoryProduct;
import commerce.emmerce.dto.CategoryProductDTO;
import commerce.emmerce.dto.SearchParamDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataR2dbcTest
class CategoryProductRepositoryTest {
    private CategoryProductRepository categoryProductRepository;
    private DatabaseClient databaseClient;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private FetchSpec<Map<String, Object>> fetchSpec;

    private CategoryProduct categoryProduct;

    @BeforeEach
    void setup() {
        categoryProduct = CategoryProduct.builder()
                .categoryProductId(1L)
                .categoryId(1L)
                .productId(1L)
                .build();

        databaseClient = mock(DatabaseClient.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        fetchSpec = mock(FetchSpec.class);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);

        categoryProductRepository = new CategoryProductRepository(databaseClient);
    }

    @Test
    void save_insert() {
        // given
        categoryProduct = CategoryProduct.builder()
                .categoryProductId(null)
                .categoryId(1L)
                .productId(1L)
                .build();

        // when
        when(executeSpec.bind("categoryId", categoryProduct.getCategoryId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", categoryProduct.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(categoryProductRepository.save(categoryProduct))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(2)).bind(anyString(), any());
    }

    @Test
    void save_update() {
        // given
        // when
        when(executeSpec.bind("categoryId", categoryProduct.getCategoryId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", categoryProduct.getProductId())).thenReturn(executeSpec);
        when(executeSpec.bind("categoryProductId", categoryProduct.getCategoryProductId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(categoryProductRepository.save(categoryProduct))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(3)).bind(anyString(), any());
    }

    @Test
    void findByProductId() {
        // given
        Map<String, Object> dataMap = Map.of(
                "category_product_id", categoryProduct.getCategoryProductId(),
                "category_id", categoryProduct.getCategoryId(),
                "product_id", categoryProduct.getProductId()
        );

        // when
        when(executeSpec.bind("productId", categoryProduct.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(dataMap));

        StepVerifier.create(categoryProductRepository.findByProductId(categoryProduct.getProductId()))
                .expectNextMatches(result ->
                        result.getCategoryProductId() == categoryProduct.getCategoryProductId() &&
                                result.getCategoryId() == categoryProduct.getCategoryId() &&
                                result.getProductId() == categoryProduct.getProductId()
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void deleteByCategoryIdAndProductId() {
        // given
        // when
        when(executeSpec.bind("categoryId", categoryProduct.getCategoryId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", categoryProduct.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(categoryProductRepository.deleteByCategoryIdAndProductId(categoryProduct.getCategoryId(), categoryProduct.getProductId()))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(2)).bind(anyString(), any());
    }

    @Test
    void findCountByCategoryId() {
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
        when(executeSpec.bind("categoryId", categoryProduct.getCategoryId())).thenReturn(executeSpec);
        when(executeSpec.bind("keyword", searchParamDTO.getKeyword())).thenReturn(executeSpec);
        when(executeSpec.bind("brand", searchParamDTO.getBrand())).thenReturn(executeSpec);
        when(executeSpec.bind("minPrice", searchParamDTO.getMinPrice())).thenReturn(executeSpec);
        when(executeSpec.bind("maxPrice", searchParamDTO.getMaxPrice())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(Map.of("count", 1L)));

        StepVerifier.create(categoryProductRepository.findCountByCategoryId(categoryProduct.getCategoryId(), searchParamDTO))
                .expectNext(1L)
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(5)).bind(anyString(), any());
    }

    @Test
    void findAllByCategoryId() {
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

        CategoryProductDTO.ListResp listResp = CategoryProductDTO.ListResp.builder()
                .productId(1L)
                .name("샴푸")
                .originalPrice(10000)
                .discountPrice(8000)
                .discountRate(20)
                .starScore(4.5)
                .titleImg("샴푸 이미지")
                .likeCount(10L)
                .brand("에비앙")
                .build();

        Map<String, Object> dataMap = Map.of(
                "product_id", 1L,
                "name", "샴푸",
                "original_price", 10000,
                "discount_price", 8000,
                "discount_rate", 20,
                "star_score", 4.5,
                "title_img", "샴푸 이미지",
                "like_count", 10L,
                "brand", "에비앙"
        );

        // when
        when(executeSpec.bind("categoryId", categoryProduct.getCategoryId())).thenReturn(executeSpec);
        when(executeSpec.bind("keyword", searchParamDTO.getKeyword())).thenReturn(executeSpec);
        when(executeSpec.bind("brand", searchParamDTO.getBrand())).thenReturn(executeSpec);
        when(executeSpec.bind("minPrice", searchParamDTO.getMinPrice())).thenReturn(executeSpec);
        when(executeSpec.bind("maxPrice", searchParamDTO.getMaxPrice())).thenReturn(executeSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(dataMap));

        StepVerifier.create(categoryProductRepository.findAllByCategoryId(categoryProduct.getCategoryId(), searchParamDTO))
                .expectNextMatches(result ->
                        result.getProductId() == listResp.getProductId() &&
                                result.getName().equals(listResp.getName()) &&
                                result.getOriginalPrice().intValue() == listResp.getOriginalPrice() &&
                                result.getDiscountPrice().intValue() == listResp.getDiscountPrice() &&
                                result.getDiscountRate().intValue() == listResp.getDiscountRate() &&
                                result.getStarScore().doubleValue() == listResp.getStarScore() &&
                                result.getTitleImg().equals(listResp.getTitleImg()) &&
                                result.getLikeCount() == listResp.getLikeCount() &&
                                result.getBrand().equals(listResp.getBrand())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(5)).bind(anyString(), any());
    }
}