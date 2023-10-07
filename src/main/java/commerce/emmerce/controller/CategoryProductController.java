package commerce.emmerce.controller;

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

    @PostMapping("/{productId}")
    public Mono<Void> enrollProductToCategory(@PathVariable Long categoryId, @PathVariable Long productId) {
        return categoryProductService.enroll(categoryId, productId);
    }

    @DeleteMapping("/{productId}")
    public Mono<Void> deleteProductToCategory(@PathVariable Long categoryId, @PathVariable Long productId) {
        return categoryProductService.cancel(categoryId, productId);
    }


    @GetMapping("/list")
    public Flux<ProductDTO.ProductListResp> findProductsByCategory(@PathVariable Long categoryId) {
        return categoryProductService.findProductList(categoryId);
    }


}
