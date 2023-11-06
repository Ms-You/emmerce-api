package commerce.emmerce.controller;

import commerce.emmerce.dto.CategoryProductDTO;
import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.SearchParamDTO;
import commerce.emmerce.service.CategoryProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "CategoryProduct", description = "카테고리 상품 관련 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/category/{categoryId}/product")
@RestController
public class CategoryProductController {

    private final CategoryProductService categoryProductService;

    @Operation(summary = "카테고리에 속한 상품 목록 조회", description = "특정 카테고리에 등록된 모든 상품 목록을 조회합니다.")
    @Parameters({ @Parameter(name = "categoryId", description = "상품을 목록을 조회할 카테고리 id"),
                @Parameter(name = "keyword", description = "상품 명 또는 상세 정보에 포함되는 키워드"),
                @Parameter(name = "brand", description = "조회하고 싶은 브랜드"),
                @Parameter(name = "limit", description = "조회 할 상품 수"),
                @Parameter(name = "minPrice", description = "상품 최소 가격"),
                @Parameter(name = "maxPrice", description = "상품 최대 가격"),
                @Parameter(name = "page", description = "페이지 번호 (기본 값: 1)"),
                @Parameter(name = "size", description = "한 페이지에 조회할 상품 수 (기본 값: 40)") })
    @GetMapping("/list")
    public Mono<PageResponseDTO<CategoryProductDTO.ListResp>> findProductsByCategory(@PathVariable Long categoryId,
                                                                                     @RequestParam(required = false) String keyword,
                                                                                     @RequestParam(required = false) String brand,
                                                                                     @RequestParam(required = false) Integer limit,
                                                                                     @RequestParam(required = false) Integer minPrice,
                                                                                     @RequestParam(required = false) Integer maxPrice,
                                                                                     @RequestParam(defaultValue = "1") Integer page,
                                                                                     @RequestParam(defaultValue = "40") Integer size) {
        SearchParamDTO searchParamDTO = SearchParamDTO.builder()
                .keyword(StringUtils.hasText(keyword) ? "%" + keyword + "%" : "%")
                .brand(StringUtils.hasText(brand) ? "%" + brand + "%" : "%")
                .limit(limit != null ? limit : Integer.MAX_VALUE)
                .minPrice(minPrice != null ? minPrice : 0)
                .maxPrice(maxPrice != null ? maxPrice : Integer.MAX_VALUE)
                .page(Math.max(1, page))
                .size(size)
                .build();

        return categoryProductService.findCategoryProductList(categoryId, searchParamDTO);
    }
}
