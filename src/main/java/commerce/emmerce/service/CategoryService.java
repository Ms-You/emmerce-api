package commerce.emmerce.service;

import commerce.emmerce.domain.Category;
import commerce.emmerce.dto.CategoryDTO;
import commerce.emmerce.repository.CategoryRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepositoryImpl categoryRepository;

    /**
     * 카테고리 추가
     * @param createReq
     * @return
     */
    public Mono<Void> create(CategoryDTO.CreateReq createReq) {
        Category category = Category.createCategory()
                .tier(createReq.getTier())
                .name(createReq.getName())
                .code(createReq.getCode())
                .parentCode(createReq.getParentCode())
                .build();

        return categoryRepository.save(category);
    }


    /**
     * 카테고리 목록 조회
     * @return
     */
    public Flux<CategoryDTO.CategoryResp> list() {
        return categoryRepository.findAll();
    }


}
