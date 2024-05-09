package commerce.emmerce.repository;

import commerce.emmerce.domain.Like;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataR2dbcTest
class LikeRepositoryTest {
    private LikeRepository likeRepository;
    private DatabaseClient databaseClient;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private FetchSpec<Map<String, Object>> fetchSpec;

    private Like like;

    @BeforeEach
    void setup() {
        like = Like.builder()
                .likeId(1L)
                .memberId(1L)
                .productId(1L)
                .build();

        databaseClient = mock(DatabaseClient.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        fetchSpec = mock(FetchSpec.class);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);

        likeRepository = new LikeRepository(databaseClient);
    }

    @Test
    void save_insert() {
        // given
        like = Like.builder()
                .likeId(null)
                .memberId(1L)
                .productId(1L)
                .build();

        // when
        when(executeSpec.bind("memberId", like.getMemberId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", like.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(likeRepository.save(like))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(2)).bind(anyString(), any());
    }

    @Test
    void save_update() {
        // given
        // when
        when(executeSpec.bind("memberId", like.getMemberId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", like.getProductId())).thenReturn(executeSpec);
        when(executeSpec.bind("likeId", like.getLikeId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(likeRepository.save(like))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(3)).bind(anyString(), any());
    }

    @Test
    void findByMemberIdAndProductId() {
        // given
        Map<String, Object> dataMap = Map.of(
                "like_id", like.getLikeId(),
                "member_id", like.getMemberId(),
                "product_id", like.getProductId()
        );

        // when
        when(executeSpec.bind("memberId", like.getMemberId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", like.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(likeRepository.findByMemberIdAndProductId(like.getMemberId(), like.getProductId()))
                .expectNextMatches(result ->
                        result.getLikeId() == like.getLikeId() &&
                                result.getMemberId() == like.getMemberId() &&
                                result.getProductId() == like.getProductId()
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(2)).bind(anyString(), any());
    }

    @Test
    void deleteByMemberIdAndProductId() {
        // given
        // when
        when(executeSpec.bind("memberId", like.getMemberId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", like.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(likeRepository.deleteByMemberIdAndProductId(like.getMemberId(), like.getProductId()))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(2)).bind(anyString(), any());
    }
}