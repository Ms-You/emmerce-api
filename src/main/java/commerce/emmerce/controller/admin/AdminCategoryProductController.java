package commerce.emmerce.controller.admin;

import commerce.emmerce.service.CategoryProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "AdminCategoryProduct", description = "카테고리 상품 관련 컨트롤러 (관리자)")
@RequiredArgsConstructor
@RequestMapping("/admin/category/{categoryId}/product")
@RestController
public class AdminCategoryProductController {

    private final CategoryProductService categoryProductService;

    @Operation(summary = "카테고리에 상품 추가", description = "특정 카테고리에 특정 상품을 추가합니다.")
    @Parameters({ @Parameter(name = "categoryId", description = "상품을 추가할 카테고리 id"),
            @Parameter(name = "productId", description = "카테고리에 추가할 상품 id") })
    @PostMapping("/{productId}")
    public Mono<Void> enrollProductToCategory(@PathVariable Long categoryId, @PathVariable Long productId) {
        return categoryProductService.enroll(categoryId, productId);
    }


    @Operation(summary = "카테고리에서 상품 제외", description = "특정 카테고리에 등록된 특정 상품을 카테고리에서 제외합니다.")
    @Parameters({ @Parameter(name = "categoryId", description = "제외할 상품이 등록된 카테고리 id"),
            @Parameter(name = "productId", description = "카테고리에서 제외할 상품 id") })
    @DeleteMapping("/{productId}")
    public Mono<Void> deleteProductToCategory(@PathVariable Long categoryId, @PathVariable Long productId) {
        return categoryProductService.cancel(categoryId, productId);
    }
}
