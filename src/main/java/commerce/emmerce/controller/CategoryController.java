package commerce.emmerce.controller;

import commerce.emmerce.dto.CategoryDTO;
import commerce.emmerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Tag(name = "Category", description = "카테고리 관련 컨트롤러")
@RequiredArgsConstructor
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 목록 조회", description = "모든 카테고리 목록을 조회합니다.")
    @GetMapping("/category/list")
    public Flux<CategoryDTO.CategoryResp> categories() {
        return categoryService.list();
    }


    @Operation(summary = "상품이 속한 카테고리 정보 조회", description = "상품이 속해 있는 카테고리 정보를 조회합니다.")
    @Parameter(name = "productId", description = "조회할 상품 id")
    @GetMapping("/product/{productId}/categories")
    public Flux<CategoryDTO.InfoResp> getCategoriesInfo(@PathVariable Long productId) {
        return categoryService.categoriesByProduct(productId);
    }

}
