package commerce.emmerce.repository;

import commerce.emmerce.domain.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class LikeRepositoryImpl {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(Like like) {
        String query = """
                insert into likes (member_id, product_id) values (:memberId, :productId)
                """;

        return databaseClient.sql(query)
                .bind("memberId", like.getMemberId())
                .bind("productId", like.getProductId())
                .then();
    }

    public Mono<Like> findByMemberIdAndProductId(Long memberId, Long productId) {
        String query = """
                select *
                from likes l
                where l.member_id = :memberId and l.product_id = :productId
                """;

        return databaseClient.sql(query)
                .bind("memberId", memberId)
                .bind("productId", productId)
                .fetch().one()
                .map(row -> Like.builder()
                        .likeId((Long) row.get("like_id"))
                        .memberId((Long) row.get("member_id"))
                        .productId((Long) row.get("product_id"))
                        .build());
    }

    public Mono<Void> delete(Like like) {
        String query = """
                delete
                from likes l
                where l.like_id = :likeId
                """;

        return databaseClient.sql(query)
                .bind("likeId", like.getLikeId())
                .then();
    }

    public Mono<Void> deleteByMemberIdAndProductId(Long memberId, Long productId) {
        String query = """
                delete
                from likes l
                where l.member_id = :memberId and l.product_id = :productId
                """;

        return databaseClient.sql(query)
                .bind("memberId", memberId)
                .bind("productId", productId)
                .then();
    }

}
