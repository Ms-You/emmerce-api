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

    public Mono<Void> create(CategoryDTO.CategoryReq categoryReq) {
        Category category = Category.createCategory()
                .tier(categoryReq.getTier())
                .name(categoryReq.getName())
                .code(categoryReq.getCode())
                .parentCode(categoryReq.getParentCode())
                .build();

        return categoryRepository.save(category);
    }

    public Flux<CategoryDTO.CategoryResp> list() {
        return categoryRepository.findAll();
    }


}
