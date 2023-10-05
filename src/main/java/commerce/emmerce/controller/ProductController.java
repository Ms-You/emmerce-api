package commerce.emmerce.controller;

import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/product")
@RestController
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public Mono<Void> createProduct(@RequestBody ProductDTO.ProductReq productReq) {
        return productService.create(productReq);
    }


    @GetMapping("/{id}")
    public Mono<ProductDTO.ProductDetailResp> productDetail(@PathVariable(value = "id") Long productId) {
        return productService.detail(productId);
    }



}
