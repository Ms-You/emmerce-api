package commerce.emmerce.controller;

import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/product")
@RestController
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public Mono<Void> createProduct(@RequestBody ProductDTO.ProductReq productReq){
        return productService.create(productReq);
    }
}
