package commerce.emmerce.repository;

import commerce.emmerce.domain.Ratings;
import commerce.emmerce.domain.Review;
import commerce.emmerce.dto.ReviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;

@RequiredArgsConstructor
@Repository
public class ReviewRepository {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(Review review) {
        String insertQuery = """
                insert into review (title, description, ratings, review_img_list, write_date, member_id, product_id, order_product_id)
                values (:title, :description, :ratings, :reviewImgList, :writeDate, :memberId, :productId, :orderProductId)
                """;

        String updateQuery = """
                update review
                set title = :title, description = :description, ratings = :ratings,
                    review_img_list = :reviewImgList, write_date = :writeDate
                where review_id = :reviewId
                """;

        String query = review.getReviewId() == null ? insertQuery : updateQuery;

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(query)
                .bind("title", review.getTitle())
                .bind("description", review.getDescription())
                .bind("ratings", review.getRatings().getValue())
                .bind("reviewImgList", review.getReviewImgList().toArray())
                .bind("writeDate", review.getWriteDate())
                .bind("memberId", review.getMemberId())
                .bind("productId", review.getProductId())
                .bind("orderProductId", review.getOrderProductId());

        if(review.getReviewId() != null) {
            executeSpec = executeSpec.bind("reviewId", review.getReviewId());
        }

        return executeSpec.then();
    }

    public Mono<Review> findById(Long reviewId) {
        String query = """
                select *
                from review r
                where r.review_id = :reviewId
                """;

        return databaseClient.sql(query)
                .bind("reviewId", reviewId)
                .fetch().one()
                .map(row -> Review.createReview()
                        .reviewId((Long) row.get("review_id"))
                        .title((String) row.get("title"))
                        .description((String) row.get("description"))
                        .ratings(Ratings.forValue((Integer) row.get("ratings")))
                        .reviewImgList(Arrays.asList((String[]) row.get("review_img_list")))
                        .writeDate((LocalDateTime) row.get("write_date"))
                        .memberId((Long) row.get("member_id"))
                        .productId((Long) row.get("product_id"))
                        .orderProductId((Long) row.get("order_product_id"))
                        .build());
    }

    public Flux<ReviewDTO.ReviewResp> findAllByProductId(Long productId) {
        String query = """
                select r.*, m.name as writer
                from review r
                inner join member m on m.member_id = r.member_id
                where product_id = :productId
                order by r.write_date desc
                """;

        return databaseClient.sql(query)
                .bind("productId", productId)
                .fetch().all()
                .map(row -> ReviewDTO.ReviewResp.builder()
                        .reviewId((Long) row.get("review_id"))
                        .title((String) row.get("title"))
                        .description((String) row.get("description"))
                        .ratings(Ratings.forValue((Integer) row.get("ratings")))
                        .reviewImgList(Arrays.asList((String[]) row.get("review_img_list")))
                        .writeDate((LocalDateTime) row.get("write_date"))
                        .memberId((Long) row.get("member_id"))
                        .writer((String) row.get("writer"))
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

    public Mono<Long> findByMemberAndOrderProduct(Long memberId, Long orderProductId) {
        String query = """
                select count(*) as count
                from review r
                where r.member_id = :memberId
                    and r.order_product_id = :orderProductId
                """;

        return databaseClient.sql(query)
                .bind("memberId", memberId)
                .bind("orderProductId", orderProductId)
                .fetch().one()
                .map(result -> (Long) result.get("count"));
    }

}
