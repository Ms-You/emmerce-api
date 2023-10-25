package commerce.emmerce.controller;

import commerce.emmerce.dto.CategoryProductDTO;
import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.service.CategoryProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "CategoryProduct", description = "카테고리 상품 관련 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/category/{categoryId}/product")
@RestController
public class CategoryProductController {

    private final CategoryProductService categoryProductService;

    @Operation(summary = "카테고리에 상품 추가 (관리자)", description = "특정 카테고리에 특정 상품을 추가합니다.")
    @Parameters({ @Parameter(name = "categoryId", description = "상품을 추가할 카테고리 id"),
                @Parameter(name = "productId", description = "카테고리에 추가할 상품 id") })
    @PostMapping("/{productId}")
    public Mono<Void> enrollProductToCategory(@PathVariable Long categoryId, @PathVariable Long productId) {
        return categoryProductService.enroll(categoryId, productId);
    }


    @Operation(summary = "카테고리에서 상품 제외 (관리자)", description = "특정 카테고리에 등록된 특정 상품을 카테고리에서 제외합니다.")
    @Parameters({ @Parameter(name = "categoryId", description = "제외할 상품이 등록된 카테고리 id"),
                @Parameter(name = "productId", description = "카테고리에서 제외할 상품 id") })
    @DeleteMapping("/{productId}")
    public Mono<Void> deleteProductToCategory(@PathVariable Long categoryId, @PathVariable Long productId) {
        return categoryProductService.cancel(categoryId, productId);
    }


    @Operation(summary = "카테고리에 속한 상품 목록 조회", description = "특정 카테고리에 등록된 모든 상품 목록을 조회합니다.")
    @Parameters({ @Parameter(name = "categoryId", description = "상품을 목록을 조회할 카테고리 id"),
                @Parameter(name = "page", description = "페이지 번호 (기본 값: 1)"),
                @Parameter(name = "size", description = "한 페이지에 조회할 상품 수 (기본 값: 40)") })
    @GetMapping("/list")
    public Mono<PageResponseDTO<CategoryProductDTO.ListResp>> findProductsByCategory(@PathVariable Long categoryId,
                                                                                                   @RequestParam(defaultValue = "1") int page,
                                                                                                   @RequestParam(defaultValue = "40") int size) {
        if(page <= 0) page = 1;
        return categoryProductService.findCategoryProductList(categoryId, page, size);
    }


}
