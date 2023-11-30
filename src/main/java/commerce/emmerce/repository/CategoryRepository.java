package commerce.emmerce.repository;

import commerce.emmerce.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class CategoryRepository {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(Category category) {
        String insertQuery = """
                insert into category (tier, name, code, parent_code)
                values (:tier, :name, :code, :parentCode)
                """;

        String updateQuery = """
                update category
                set tier = :tier, name = :name, code = :code, parent_code = :parentCode
                where category_id = :categoryId
                """;

        String query = category.getCategoryId() == null ? insertQuery : updateQuery;

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(query)
                .bind("tier", category.getTier())
                .bind("name", category.getName())
                .bind("code", category.getCode())
                .bind("parentCode", category.getParentCode());

        if(category.getCategoryId() != null) {
            executeSpec = executeSpec.bind("categoryId", category.getCategoryId());
        }

        return executeSpec.then();
    }

    public Flux<Category> findAll() {
        String query = """
                select *
                from category c
                """;
        return databaseClient.sql(query)
                .fetch().all()
                .map(row -> Category.createCategory()
                        .categoryId((Long) row.get("category_id"))
                        .tier((Integer) row.get("tier"))
                        .name((String) row.get("name"))
                        .code((String) row.get("code"))
                        .parentCode((String) row.get("parent_code"))
                        .build());
    }

    public Mono<Category> findById(Long categoryId) {
        String query = """
                select *
                from category c
                where c.category_id = :categoryId
                """;

        return databaseClient.sql(query)
                .bind("categoryId", categoryId)
                .fetch().one()
                .map(row -> Category.createCategory()
                        .categoryId((Long) row.get("category_id"))
                        .tier((Integer) row.get("tier"))
                        .name((String) row.get("name"))
                        .code((String) row.get("code"))
                        .parentCode((String) row.get("parent_code"))
                        .build());
    }

    public Mono<Category> findByParentCode(String parentCode) {
        String query = """
                select *
                from category c
                where c.code = :parentCode
                """;

        return databaseClient.sql(query)
                .bind("parentCode", parentCode)
                .fetch().one()
                .map(row -> Category.createCategory()
                        .categoryId((Long) row.get("category_id"))
                        .tier((Integer) row.get("tier"))
                        .name((String) row.get("name"))
                        .code((String) row.get("code"))
                        .parentCode((String) row.get("parent_code"))
                        .build());
    }

}
