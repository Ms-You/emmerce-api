package commerce.emmerce.service;

import commerce.emmerce.domain.CategoryProduct;
import commerce.emmerce.dto.CategoryProductDTO;
import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.SearchParamDTO;
import commerce.emmerce.repository.CategoryProductRepositoryImpl;
import commerce.emmerce.repository.CategoryRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class CategoryProductService {

    private final CategoryProductRepositoryImpl categoryProductRepository;
    private final CategoryRepositoryImpl categoryRepository;

    /**
     * 카테고리에 상품 등록 (상위 카테고리에도 포함)
     * @param categoryId
     * @param productId
     * @return
     */
    public Mono<Void> enroll(Long categoryId, Long productId) {
        return saveCategoryProduct(categoryId, productId)
                .then(categoryRepository.findById(categoryId))
                .flatMap(category -> {
                    if(!category.getParentCode().equals("-")) {
                        return categoryRepository.findByParentCode(category.getParentCode())
                                .flatMap(parentCategory -> enroll(parentCategory.getCategoryId(), productId));
                    } else {
                        return Mono.empty();
                    }
                });
    }

    private Mono<Void> saveCategoryProduct(Long categoryId, Long productId) {
        return categoryProductRepository.save(CategoryProduct.builder()
                .categoryId(categoryId)
                .productId(productId)
                .build());
    }


    /**
     * 카테고리에서 상품 제거
     * @param categoryId
     * @param productId
     * @return
     */
    public Mono<Void> cancel(Long categoryId, Long productId) {
        // 예외 처리
        return categoryProductRepository.deleteByCategoryIdAndProductId(categoryId, productId);
    }


    /**
     * 카테고리에 속한 상품 목록 조회
     * @param categoryId
     * @param searchParamDTO
     * @return
     */
    public Mono<PageResponseDTO<CategoryProductDTO.ListResp>> findCategoryProductList(Long categoryId, SearchParamDTO searchParamDTO) {
        int page = searchParamDTO.getPage();
        int size = searchParamDTO.getSize();

        return categoryProductRepository.findCountByCategoryId(categoryId, searchParamDTO)
                .flatMap(totalElements -> categoryProductRepository.findAllByCategoryId(categoryId, searchParamDTO)
                        .skip((page-1) * size)
                        .take(size)
                        .collectList()
                        .map(content -> new PageResponseDTO<>(content, page, size, totalElements.intValue()))
                );
    }

}
