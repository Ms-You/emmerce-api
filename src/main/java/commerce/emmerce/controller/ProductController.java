package commerce.emmerce.controller;

import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Product", description = "상품 관련 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/product")
@RestController
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "새로운 상품 추가 (관리자)", description = "새로운 상품을 등록합니다.")
    @Parameter(name = "productReq", description = "상품 명, 상세 정보 등 상품 정보를 전달")
    @PostMapping
    public Mono<Void> createProduct(@RequestBody ProductDTO.ProductReq productReq) {
        return productService.create(productReq);
    }


    @Operation(summary = "상품 상세 정보 조회", description = "상품의 상세 정보 및 작성된 리뷰를 조회합니다.")
    @Parameter(name = "productId", description = "조회할 상품 id")
    @GetMapping("/{productId}")
    public Mono<ProductDTO.DetailResp> productDetail(@PathVariable Long productId) {
        return productService.detail(productId);
    }


    @Operation(summary = "상품 정보 수정 (관리자)", description = "상품과 관련된 정보를 수정합니다.")
    @Parameters({ @Parameter(name = "productId", description = "수정할 상품 id"),
                @Parameter(name = "updateReq", description = "수정할 상품 정보") })
    @PutMapping("/{productId}")
    public Mono<Void> updateProduct(@PathVariable Long productId, @RequestBody ProductDTO.UpdateReq updateReq) {
        return productService.update(productId, updateReq);
    }


    @Operation(summary = "최신 상품 목록 조회", description = "가장 최근에 등록된 상품 목록을 조회합니다.")
    @Parameter(name = "size", description = "조회할 최신 상품 개수 (기본 값: 12)")
    @GetMapping("/latest")
    public Flux<ProductDTO.ListResp> latestProducts(@RequestParam(defaultValue = "12") int size) {
        return productService.latest(size);
    }


    @Operation(summary = "상품 검색", description = "조건에 맞는 특정 상품을 조회합니다.")
    @Parameters({ @Parameter(name = "keyword", description = "상품 명 또는 상세 정보에 포함되는 키워드"),
                @Parameter(name = "brand", description = "조회하고 싶은 브랜드"),
                @Parameter(name = "limit", description = "조회 할 상품 수"),
                @Parameter(name = "minPrice", description = "상품 최소 가격"),
                @Parameter(name = "maxPrice", description = "상품 최대 가격"),
                @Parameter(name = "page", description = "페이지 번호 (기본 값: 1)"),
                @Parameter(name = "size", description = "한 페이지에 조회할 상품 수 (기본 값: 40)") })
    @GetMapping("/search")
    public Mono<PageResponseDTO<ProductDTO.ListResp>> searchProducts(@RequestParam String keyword, @RequestParam String brand,
                                                                     @RequestParam int limit, @RequestParam int minPrice,
                                                                     @RequestParam int maxPrice, @RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "40") int size) {
        if (page <= 0) page = 1;
        return productService.search(keyword, brand, limit, minPrice, maxPrice, page, size);
    }


    @Operation(summary = "할인률 큰 상품 목록 조회", description = "할인률이 가장 큰 상품 목록을 조회합니다.")
    @Parameter(name = "size", description = "조회할 상품 개수 (기본 값: 15)")
    @GetMapping("/hot-deal")
    public Flux<ProductDTO.ListResp> hotDeal(@RequestParam(defaultValue = "15") int size) {
        return productService.hotDeal(size);
    }
}
