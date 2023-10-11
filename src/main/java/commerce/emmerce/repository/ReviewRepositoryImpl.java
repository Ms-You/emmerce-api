package commerce.emmerce.repository;

import commerce.emmerce.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

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

}
