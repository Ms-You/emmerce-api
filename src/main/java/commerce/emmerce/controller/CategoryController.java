package commerce.emmerce.controller;

import commerce.emmerce.dto.CategoryDTO;
import commerce.emmerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Category", description = "카테고리 관련 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/category")
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 추가 (관리자)", description = "카테고리를 추가합니다.")
    @Parameter(name = "createReq", description = "카테고리 계층, 이름, 코드, 상위 코드를 입력받아 새로운 카테고리를 생성합니다.")
    @PostMapping
    public Mono<Void> createCategory(@RequestBody CategoryDTO.CreateReq createReq) {
        return categoryService.create(createReq);
    }


    @Operation(summary = "카테고리 목록 조회", description = "모든 카테고리 목록을 조회합니다.")
    @GetMapping("/list")
    public Flux<CategoryDTO.CategoryResp> categories() {
        return categoryService.list();
    }


}
