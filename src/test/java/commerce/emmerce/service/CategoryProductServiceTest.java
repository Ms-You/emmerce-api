package commerce.emmerce.service;

import commerce.emmerce.domain.Category;
import commerce.emmerce.domain.CategoryProduct;
import commerce.emmerce.domain.Product;
import commerce.emmerce.dto.CategoryProductDTO;
import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.SearchParamDTO;
import commerce.emmerce.repository.CategoryProductRepository;
import commerce.emmerce.repository.CategoryRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(CategoryProductService.class)
class CategoryProductServiceTest {
    @Autowired
    private CategoryProductService categoryProductService;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private CategoryProductRepository categoryProductRepository;

    private Category category;
    private Product product;

    @BeforeEach
    void setup() {
        category = Category.createCategory()
                .categoryId(1L)
                .tier(1)
                .name("캐주얼/유니섹스")
                .code("10000")
                .parentCode("-")
                .build();

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
    }


    @Test
    @DisplayName("카테고리에 상품 등록 테스트 - 하위 카테고리 존재")
    void enroll_case1() {
        // given
        Category childCategory = Category.createCategory()
                .categoryId(2L)
                .tier(2)
                .name("바지")
                .code("10100")
                .parentCode("10000")
                .build();

        // when
        when(categoryProductRepository.save(any(CategoryProduct.class))).thenReturn(Mono.empty());
        when(categoryRepository.findById(category.getCategoryId())).thenReturn(Mono.just(category));
        when(categoryRepository.findById(childCategory.getCategoryId())).thenReturn(Mono.just(childCategory));
        when(categoryRepository.findByParentCode(category.getParentCode())).thenReturn(Mono.empty());
        when(categoryRepository.findByParentCode(childCategory.getParentCode())).thenReturn(Mono.just(category));

        StepVerifier.create(categoryProductService.enroll(childCategory.getCategoryId(), product.getProductId()))
                .verifyComplete();

        // then
        verify(categoryProductRepository, times(2)).save(any(CategoryProduct.class));
        verify(categoryRepository, times(2)).findById(anyLong());
        verify(categoryRepository, times(1)).findByParentCode(anyString());
    }

    @Test
    @DisplayName("카테고리에 상품 등록 테스트 - 최상위 카테고리")
    void enroll_case2() {
        // given
        // when
        when(categoryProductRepository.save(any(CategoryProduct.class))).thenReturn(Mono.empty());
        when(categoryRepository.findById(category.getCategoryId())).thenReturn(Mono.just(category));
        when(categoryRepository.findByParentCode(category.getParentCode())).thenReturn(Mono.empty());

        StepVerifier.create(categoryProductService.enroll(category.getCategoryId(), product.getProductId()))
                .verifyComplete();

        // then
        verify(categoryProductRepository, times(1)).save(any(CategoryProduct.class));
        verify(categoryRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("카테고리에서 상품 제거 테스트")
    void cancel() {
        // given
        // when
        when(categoryProductRepository.deleteByCategoryIdAndProductId(category.getCategoryId(), product.getProductId())).thenReturn(Mono.empty());

        StepVerifier.create(categoryProductService.cancel(category.getCategoryId(), product.getProductId()))
                .verifyComplete();

        // then
        verify(categoryProductRepository, times(1)).deleteByCategoryIdAndProductId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("카테고리에 속한 상품 목록 조회 테스트")
    void findCategoryProductList() {
        // given
        CategoryProductDTO.ListResp listResp = CategoryProductDTO.ListResp.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .originalPrice(product.getOriginalPrice())
                .discountPrice(product.getDiscountPrice())
                .discountRate(product.getDiscountRate())
                .starScore(product.getStarScore())
                .titleImg(product.getTitleImg())
                .likeCount(10L)
                .brand(product.getBrand())
                .build();

        PageResponseDTO<CategoryProductDTO.ListResp> pageResponseDTO = new PageResponseDTO<>(List.of(listResp), 1, 1, 1, true, true);

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
        when(categoryProductRepository.findCountByCategoryId(category.getCategoryId(), searchParamDTO)).thenReturn(Mono.just(1L));
        when(categoryProductRepository.findAllByCategoryId(category.getCategoryId(), searchParamDTO)).thenReturn(Flux.just(listResp));

        StepVerifier.create(categoryProductService.findCategoryProductList(category.getCategoryId(), searchParamDTO))
                .expectNextMatches(result -> result.getContent().get(0).getProductId() == pageResponseDTO.getContent().get(0).getProductId() &&
                        result.getContent().get(0).getName().equals(pageResponseDTO.getContent().get(0).getName()) &&
                        result.getContent().get(0).getOriginalPrice() == pageResponseDTO.getContent().get(0).getOriginalPrice() &&
                        result.getContent().get(0).getDiscountPrice() == pageResponseDTO.getContent().get(0).getDiscountPrice() &&
                        result.getContent().get(0).getDiscountRate() == pageResponseDTO.getContent().get(0).getDiscountRate() &&
                        result.getContent().get(0).getStarScore() == pageResponseDTO.getContent().get(0).getStarScore() &&
                        result.getContent().get(0).getTitleImg().equals(pageResponseDTO.getContent().get(0).getTitleImg()) &&
                        result.getContent().get(0).getLikeCount() == pageResponseDTO.getContent().get(0).getLikeCount() &&
                        result.getContent().get(0).getBrand().equals(pageResponseDTO.getContent().get(0).getBrand()) &&
                        result.getPageNumber() == searchParamDTO.getPage() &&
                        result.getTotalPages() == pageResponseDTO.getTotalPages() &&
                        result.getTotalElements() == pageResponseDTO.getTotalElements() &&
                        result.isFirst() == pageResponseDTO.isFirst() &&
                        result.isLast() == pageResponseDTO.isLast()
                ).verifyComplete();

        // then
        verify(categoryProductRepository, times(1)).findCountByCategoryId(anyLong(), any(SearchParamDTO.class));
        verify(categoryProductRepository, times(1)).findAllByCategoryId(anyLong(), any(SearchParamDTO.class));
    }
}