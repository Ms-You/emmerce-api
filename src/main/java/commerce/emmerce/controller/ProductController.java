package commerce.emmerce.controller;

import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/product")
@RestController
public class ProductController {

    private final ProductService productService;

    /**
     * 새로운 상품 추가
     * @param productReq
     * @return
     */
    @PostMapping
    public Mono<Void> createProduct(@RequestBody ProductDTO.ProductReq productReq) {
        return productService.create(productReq);
    }


    /**
     * 상품 상세 정보 조회
     * @param productId
     * @return
     */
    @GetMapping("/{productId}")
    public Mono<ResponseEntity<ProductDTO.ProductDetailResp>> productDetail(@PathVariable Long productId) {
        return productService.detail(productId)
                .map(resp -> ResponseEntity.ok(resp))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    /**
     * 상품 정보 수정 (관리자)
     * @param productId
     * @return
     */
    @PutMapping("/{productId}")
    public Mono<ResponseEntity> updateProduct(@PathVariable Long productId, @RequestBody ProductDTO.ProductUpdateReq productUpdateReq) {
        return productService.update(productId, productUpdateReq)
                .then(Mono.just(new ResponseEntity(HttpStatus.ACCEPTED)))
                .onErrorReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));
    }


    /**
     * 최신 상품 목록 조회
     * @return
     */
    @GetMapping("/latest")
    public Flux<ProductDTO.ProductListResp> latestProducts() {
        return productService.latest();
    }


    /**
     * 상품 검색
     * @param keyword
     * @param brand
     * @param limit
     * @param minPrice
     * @param maxPrice
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/search")
    public Mono<PageResponseDTO<ProductDTO.ProductListResp>> searchProducts(@RequestParam String keyword, @RequestParam String brand,
                                                                           @RequestParam int limit, @RequestParam int minPrice,
                                                                           @RequestParam int maxPrice, @RequestParam(defaultValue = "1") int page,
                                                                           @RequestParam(defaultValue = "40") int size) {
        if(page <= 0) page = 1;
        return productService.search(keyword, brand, limit, minPrice, maxPrice, page, size);
    }


    /**
     * 할인률 큰 상품 목록 조회
     * @return
     */
    @GetMapping("/hot-deal")
    public Flux<ProductDTO.ProductListResp> hotDeal() {
        return productService.hotDeal();
    }

}
