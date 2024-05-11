package commerce.emmerce.service;

import commerce.emmerce.domain.Category;
import commerce.emmerce.domain.CategoryProduct;
import commerce.emmerce.dto.CategoryDTO;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(CategoryService.class)
class CategoryServiceTest {
    @Autowired
    private CategoryService categoryService;

    @MockBean
    private CategoryRepository categoryRepository;

    @MockBean
    private CategoryProductRepository categoryProductRepository;

    private Category category;
    private CategoryProduct categoryProduct;

    @BeforeEach
    void setup() {
        category = Category.createCategory()
                .categoryId(1L)
                .tier(1)
                .name("캐주얼/유니섹스")
                .code("10000")
                .parentCode("-")
                .build();

        categoryProduct = CategoryProduct.builder()
                .categoryProductId(1L)
                .categoryId(category.getCategoryId())
                .productId(1L)
                .build();
    }

    @Test
    @DisplayName("카테고리 추가 테스트")
    void create() {
        // given
        CategoryDTO.CreateReq createReq = new CategoryDTO.CreateReq(1, "캐주얼/유니섹스", "10000", "-");

        // when
        when(categoryRepository.save(any(Category.class))).thenReturn(Mono.empty());

        StepVerifier.create(categoryService.create(createReq))
                .verifyComplete();

        // then
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 목록 조회 테스트")
    void list() {
        // given
        CategoryDTO.CategoryResp categoryResp = CategoryDTO.CategoryResp.builder()
                .categoryId(category.getCategoryId())
                .tier(category.getTier())
                .name(category.getName())
                .code(category.getCode())
                .parentCode(category.getParentCode())
                .build();

        // when
        when(categoryRepository.findAll()).thenReturn(Flux.just(category));

        StepVerifier.create(categoryService.list())
                .expectNextMatches(result ->
                        result.getCategoryId() == categoryResp.getCategoryId() &&
                                result.getTier() == categoryResp.getTier() &&
                                result.getName().equals(categoryResp.getName()) &&
                                result.getCode().equals(categoryResp.getCode()) &&
                                result.getParentCode().equals(categoryResp.getParentCode()))
                .verifyComplete();

        // then
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("상품이 속한 카테고리 정보 조회 테스트")
    void categoriesByProduct() {
        // given
        CategoryDTO.InfoResp infoResp =
                new CategoryDTO.InfoResp(category.getCategoryId(), category.getTier(), category.getName());

        // when
        when(categoryProductRepository.findByProductId(categoryProduct.getProductId())).thenReturn(Flux.just(categoryProduct));
        when(categoryRepository.findById(categoryProduct.getCategoryId())).thenReturn(Mono.just(category));

        StepVerifier.create(categoryService.categoriesByProduct(categoryProduct.getProductId()))
                .expectNextMatches(result ->
                        result.getCategoryId() == infoResp.getCategoryId() &&
                                result.getTier() == infoResp.getTier() &&
                                result.getName().equals(infoResp.getName()))
                .verifyComplete();

        // then
        verify(categoryProductRepository, times(1)).findByProductId(anyLong());
        verify(categoryRepository, times(1)).findById(anyLong());
    }
}