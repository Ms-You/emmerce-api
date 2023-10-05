package commerce.emmerce.repository;

import commerce.emmerce.domain.Category;
import commerce.emmerce.dto.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class CategoryRepositoryImpl {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(Category category) {
        String query = """
                insert into category (tier, name, code, parent_code) values(:tier, :name, :code, :parentCode)
                """;
        return databaseClient.sql(query)
                .bind("tier", category.getTier())
                .bind("name", category.getName())
                .bind("code", category.getCode())
                .bind("parentCode", category.getParentCode())
                .then();
    }

    public Flux<CategoryDTO.CategoryResp> findAll() {
        String query = """
                select * from category c
                """;
        return databaseClient.sql(query)
                .fetch().all()
                .map(row -> CategoryDTO.CategoryResp.builder()
                        .categoryId((Long) row.get("category_id"))
                        .tier((Integer) row.get("tier"))
                        .name((String) row.get("name"))
                        .code((String) row.get("code"))
                        .parentCode((String) row.get("parent_code"))
                        .build());
    }

}
