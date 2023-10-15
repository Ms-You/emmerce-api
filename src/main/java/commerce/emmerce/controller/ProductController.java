package commerce.emmerce.controller;

import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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


    @GetMapping("/{productId}")
    public Mono<ResponseEntity<ProductDTO.ProductDetailResp>> productDetail(@PathVariable Long productId) {
        return productService.detail(productId)
                .map(resp -> ResponseEntity.ok(resp))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }



}
