package commerce.emmerce.controller.admin;

import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "AdminProduct", description = "상품 관련 컨트롤러 (관리자)")
@RequiredArgsConstructor
@RequestMapping("/admin/product")
@RestController
public class AdminProductController {

    private final ProductService productService;

    @Operation(summary = "새로운 상품 추가", description = "새로운 상품을 등록합니다.")
    @Parameters({ @Parameter(name = "productReq", description = "상품 명, 상세 정보 등 상품 정보를 전달"),
                @Parameter(name = "titleImage", description = "상품의 대표 이미지를 전달"),
                @Parameter(name = "detailImages", description = "상품의 이미지 목록을 전달") })
    @PostMapping
    public Mono<Void> createProduct(@RequestPart("productReq") Mono<ProductDTO.ProductReq> productReqMono,
                                    @RequestPart("titleImage") Mono<FilePart> titleImage,
                                    @RequestPart("detailImages") Flux<FilePart> detailImages) {
        return productService.create(productReqMono, titleImage, detailImages);
    }

    @Operation(summary = "상품 정보 수정", description = "상품과 관련된 정보를 수정합니다.")
    @Parameters({ @Parameter(name = "productId", description = "수정할 상품 id"),
                @Parameter(name = "updateReq", description = "수정할 상품 정보를 전달"),
                @Parameter(name = "titleImage", description = "수정할 상품의 대표 이미지를 전달"),
                @Parameter(name = "detailImages", description = "수정할 상품의 이미지 목록을 전달") })
    @PutMapping("/{productId}")
    public Mono<Void> updateProduct(@PathVariable Long productId,
                                    @RequestPart("updateReq") Mono<ProductDTO.UpdateReq> updateReqMono,
                                    @RequestPart("titleImage") Mono<FilePart> titleImage,
                                    @RequestPart("detailImages") Flux<FilePart> detailImages) {
        return productService.update(productId, updateReqMono, titleImage, detailImages);
    }


}
