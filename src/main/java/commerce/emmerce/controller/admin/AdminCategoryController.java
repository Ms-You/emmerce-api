package commerce.emmerce.controller.admin;

import commerce.emmerce.dto.CategoryDTO;
import commerce.emmerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "AdminCategory", description = "카테고리 관련 컨트롤러 (관리자)")
@RequiredArgsConstructor
@RequestMapping("/admin/category")
@RestController
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 추가", description = "카테고리를 추가합니다.")
    @Parameter(name = "createReq", description = "카테고리 계층, 이름, 코드, 상위 코드를 입력받아 새로운 카테고리를 생성합니다.")
    @PostMapping
    public Mono<Void> createCategory(@RequestBody CategoryDTO.CreateReq createReq) {
        return categoryService.create(createReq);
    }

}
