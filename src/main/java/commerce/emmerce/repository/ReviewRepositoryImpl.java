package commerce.emmerce.repository;

import commerce.emmerce.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;

@RequiredArgsConstructor
@Repository
public class ReviewRepositoryImpl {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(Review review) {
        String query = """
                insert into review (title, description, star_score, review_img_list, write_date, member_id, product_id) 
                values (:title, :description, :starScore, :reviewImgList, :writeDate, :memberId, :productId)
                """;

        return databaseClient.sql(query)
                .bind("title", review.getTitle())
                .bind("description", review.getDescription())
                .bind("starScore", review.getStarScore())
                .bind("reviewImgList", review.getReviewImgList().toArray())
                .bind("writeDate", review.getWriteDate())
                .bind("memberId", review.getMemberId())
                .bind("productId", review.getProductId())
                .then();
    }


    public Flux<Review> findAllByProductId(Long productId) {
        String query = """
                select *
                from review r
                where r.product_id = :productId
                """;

        return databaseClient.sql(query)
                .bind("productId", productId)
                .fetch().all()
                .map(row -> Review.createReview()
                        .reviewId((Long) row.get("review_id"))
                        .title((String) row.get("title"))
                        .description((String) row.get("description"))
                        .startScore((Double) row.get("star_score"))
                        .reviewImgList(Arrays.asList((String[]) row.get("review_img_list")))
                        .writeDate((LocalDate) row.get("write_date"))
                        .memberId((Long) row.get("member_id"))
                        .productId((Long) row.get("product_id"))
                        .build());
    }


    public Mono<Long> deleteById(Long reviewId) {
        String query = """
                delete
                from review r
                where r.review_id = :reviewId
                """;

        return databaseClient.sql(query)
                .bind("reviewId", reviewId)
                .fetch().rowsUpdated();
    }


    public Mono<Long> reviewCountByProduct(Long productId) {
        String query = """
                select count(*) as count
                from review r
                where r.product_id = :productId
                """;

        return databaseClient.sql(query)
                .bind("productId", productId)
                .fetch().one()
                .map(result -> (Long) result.get("count"));
    }


    public Flux<Review> reviewsByProduct(Long productId) {
        String query = """
                select *
                from review r
                where r.product_id = :productId
                """;

        return databaseClient.sql(query)
                .bind("productId", productId)
                .fetch().all()
                .map(row -> Review.createReview()
                        .reviewId((Long) row.get("review_id"))
                        .title((String) row.get("title"))
                        .description((String) row.get("description"))
                        .startScore((Double) row.get("star_score"))
                        .reviewImgList(Arrays.asList((String[]) row.get("review_img_list")))
                        .writeDate((LocalDate) row.get("write_date"))
                        .memberId((Long) row.get("member_id"))
                        .productId((Long) row.get("product_id"))
                        .build());
    }
}
