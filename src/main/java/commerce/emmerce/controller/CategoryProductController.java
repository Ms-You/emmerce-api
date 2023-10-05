package commerce.emmerce.controller;

import commerce.emmerce.dto.CategoryProductDTO;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.service.CategoryProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/category-product")
@RestController
public class CategoryProductController {

    private final CategoryProductService categoryProductService;

    @PostMapping
    public Mono<Void> enrollProductToCategory(@RequestBody CategoryProductDTO.CategoryProductReq categoryProductReq) {
        return categoryProductService.enroll(categoryProductReq);
    }

    @DeleteMapping
    public Mono<Void> deleteProductToCategory(@RequestBody CategoryProductDTO.CategoryProductReq categoryProductReq) {
        return categoryProductService.cancel(categoryProductReq);
    }


    @GetMapping("/category/{id}")
    public Flux<ProductDTO.ProductListResp> findProductsByCategory(@PathVariable(value = "id") Long categoryId) {
        return categoryProductService.findProductList(categoryId);
    }


}
