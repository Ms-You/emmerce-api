package commerce.emmerce.repository;

import commerce.emmerce.domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataR2dbcTest
class CategoryRepositoryTest {
    private CategoryRepository categoryRepository;
    private DatabaseClient databaseClient;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private FetchSpec<Map<String, Object>> fetchSpec;

    private Category category;

    @BeforeEach
    void setup() {
        category = Category.createCategory()
                .categoryId(1L)
                .tier(1)
                .name("캐주얼/유니섹스")
                .code("10000")
                .parentCode("-")
                .build();

        databaseClient = mock(DatabaseClient.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        fetchSpec = mock(FetchSpec.class);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);

        categoryRepository = new CategoryRepository(databaseClient);
    }

    @Test
    void save_insert() {
        // given
        category = Category.createCategory()
                .categoryId(null)
                .tier(1)
                .name("캐주얼/유니섹스")
                .code("10000")
                .parentCode("-")
                .build();

        // when
        when(executeSpec.bind("tier", category.getTier())).thenReturn(executeSpec);
        when(executeSpec.bind("name", category.getName())).thenReturn(executeSpec);
        when(executeSpec.bind("code", category.getCode())).thenReturn(executeSpec);
        when(executeSpec.bind("parentCode", category.getParentCode())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(categoryRepository.save(category))
                .verifyComplete();

        // then
        verify(databaseClient, atLeastOnce()).sql(anyString());
        verify(executeSpec, times(4)).bind(anyString(), any());
    }

    @Test
    void save_update() {
        // given
        // when
        when(executeSpec.bind("tier", category.getTier())).thenReturn(executeSpec);
        when(executeSpec.bind("name", category.getName())).thenReturn(executeSpec);
        when(executeSpec.bind("code", category.getCode())).thenReturn(executeSpec);
        when(executeSpec.bind("parentCode", category.getParentCode())).thenReturn(executeSpec);
        when(executeSpec.bind("categoryId", category.getCategoryId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(categoryRepository.save(category))
                .verifyComplete();

        // then
        verify(databaseClient, atLeastOnce()).sql(anyString());
        verify(executeSpec, times(5)).bind(anyString(), any());
    }

    @Test
    void findAll() {
        // given
        Map<String, Object> dataMap = Map.of(
                "category_id", category.getCategoryId(),
                "tier", category.getTier(),
                "name", category.getName(),
                "code", category.getCode(),
                "parent_code", category.getParentCode()
        );

        // when
        when(fetchSpec.all()).thenReturn(Flux.just(dataMap));

        StepVerifier.create(categoryRepository.findAll())
                .expectNextMatches(result ->
                        result.getCategoryId() == category.getCategoryId() &&
                                result.getTier() == category.getTier() &&
                                result.getName().equals(category.getName()) &&
                                result.getCode().equals(category.getCode()) &&
                                result.getParentCode().equals(category.getParentCode())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
    }

    @Test
    void findById() {
        // given
        Map<String, Object> dataMap = Map.of(
                "category_id", category.getCategoryId(),
                "tier", category.getTier(),
                "name", category.getName(),
                "code", category.getCode(),
                "parent_code", category.getParentCode()
        );

        // when
        when(executeSpec.bind("categoryId", category.getCategoryId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(categoryRepository.findById(category.getCategoryId()))
                .expectNextMatches(result ->
                        result.getCategoryId() == category.getCategoryId() &&
                                result.getTier() == category.getTier() &&
                                result.getName().equals(category.getName()) &&
                                result.getCode().equals(category.getCode()) &&
                                result.getParentCode().equals(category.getParentCode())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void findByParentCode() {
        // given
        Category childCategory = Category.createCategory()
                .categoryId(2L)
                .tier(2)
                .name("청바지")
                .code("10100")
                .parentCode("10000")
                .build();

        Map<String, Object> dataMap = Map.of(
                "category_id", category.getCategoryId(),
                "tier", category.getTier(),
                "name", category.getName(),
                "code", category.getCode(),
                "parent_code", category.getParentCode()
        );

        // when
        when(executeSpec.bind("parentCode", childCategory.getParentCode())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(categoryRepository.findByParentCode(childCategory.getParentCode()))
                .expectNextMatches(result ->
                        result.getCategoryId() == category.getCategoryId() &&
                                result.getTier() == category.getTier() &&
                                result.getName().equals(category.getName()) &&
                                result.getCode().equals(category.getCode()) &&
                                result.getParentCode().equals(category.getParentCode())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }
}