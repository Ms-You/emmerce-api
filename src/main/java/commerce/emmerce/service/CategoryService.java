package commerce.emmerce.service;

import commerce.emmerce.domain.Category;
import commerce.emmerce.dto.CategoryDTO;
import commerce.emmerce.repository.CategoryProductRepository;
import commerce.emmerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryProductRepository categoryProductRepository;

    /**
     * 카테고리 추가
     * @param createReq
     * @return
     */
    public Mono<Void> create(CategoryDTO.CreateReq createReq) {
        Category category = Category.createCategory()
                .tier(createReq.getTier())
                .name(createReq.getName())
                .code(createReq.getCode())
                .parentCode(createReq.getParentCode())
                .build();

        return categoryRepository.save(category);
    }

    /**
     * 카테고리 목록 조회
     * @return
     */
    public Flux<CategoryDTO.CategoryResp> list() {
        return categoryRepository.findAll()
                .map(category -> CategoryDTO.CategoryResp.builder()
                        .categoryId(category.getCategoryId())
                        .tier(category.getTier())
                        .name(category.getName())
                        .code(category.getCode())
                        .parentCode(category.getParentCode())
                        .build());
    }

    /**
     * 상품이 속한 카테고리 정보 조회
     * @param productId
     * @return
     */
    public Flux<CategoryDTO.InfoResp> categoriesByProduct(Long productId) {
        return categoryProductRepository.findByProductId(productId)
                .flatMap(categoryProduct -> categoryRepository.findById(categoryProduct.getCategoryId()))
                .map(category -> CategoryDTO.InfoResp.builder()
                        .categoryId(category.getCategoryId())
                        .tier(category.getTier())
                        .name(category.getName())
                        .build()
                );
    }

}
