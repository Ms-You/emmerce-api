package commerce.emmerce.controller;

import commerce.emmerce.dto.CategoryDTO;
import commerce.emmerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/category")
@RestController
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 카테고리 추가
     * @param createReq
     * @return
     */
    @PostMapping
    public Mono<Void> createCategory(@RequestBody CategoryDTO.CreateReq createReq) {
        return categoryService.create(createReq);
    }

    /**
     * 카테고리 목록 조회
     * @return
     */
    @GetMapping("/list")
    public Flux<CategoryDTO.CategoryResp> categories() {
        return categoryService.list();
    }


}
