package commerce.emmerce.service;

import commerce.emmerce.domain.CategoryProduct;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.repository.CategoryProductRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class CategoryProductService {

    private final CategoryProductRepositoryImpl categoryProductRepository;

    /**
     * 카테고리에 상품 등록
     * @param categoryId
     * @param productId
     * @return
     */
    public Mono<Void> enroll(Long categoryId, Long productId) {
        CategoryProduct categoryProduct = CategoryProduct.builder()
                .categoryId(categoryId)
                .productId(productId)
                .build();

        return categoryProductRepository.save(categoryProduct);
    }


    /**
     * 카테고리에서 상품 제거
     * @param categoryId
     * @param productId
     * @return
     */
    public Mono<Void> cancel(Long categoryId, Long productId) {
        return categoryProductRepository.deleteByCategoryIdAndProductId(categoryId, productId);
    }


    /**
     * 카테고리에 속한 상품 목록 조회
     * @param categoryId
     * @return
     */
    public Flux<ProductDTO.ProductListResp> findProductList(Long categoryId) {
        return categoryProductRepository.productListByCategoryId(categoryId);
    }
}
