package commerce.emmerce.controller;

import commerce.emmerce.dto.CategoryProductDTO;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.service.CategoryProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/category/{categoryId}/product")
@RestController
public class CategoryProductController {

    private final CategoryProductService categoryProductService;

    /**
     * 카테고리에 상품 추가 (관리자)
     * @param categoryId
     * @param productId
     * @return
     */
    @PostMapping("/{productId}")
    public Mono<Void> enrollProductToCategory(@PathVariable Long categoryId, @PathVariable Long productId) {
        return categoryProductService.enroll(categoryId, productId);
    }


    /**
     * 카테고리에서 상품 제거 (관리자)
     * @param categoryId
     * @param productId
     * @return
     */
    @DeleteMapping("/{productId}")
    public Mono<Void> deleteProductToCategory(@PathVariable Long categoryId, @PathVariable Long productId) {
        return categoryProductService.cancel(categoryId, productId);
    }


    /**
     * 카테고리에 속한 상품 목록 조회
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Flux<CategoryProductDTO.CategoryProductListResp> findProductsByCategory(@PathVariable Long categoryId) {
        return categoryProductService.findCategoryProductList(categoryId);
    }


}
