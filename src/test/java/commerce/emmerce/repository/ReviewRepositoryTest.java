package commerce.emmerce.repository;

import commerce.emmerce.domain.Ratings;
import commerce.emmerce.domain.Review;
import commerce.emmerce.dto.ReviewDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataR2dbcTest
class ReviewRepositoryTest {
    private ReviewRepository reviewRepository;
    private DatabaseClient databaseClient;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private FetchSpec<Map<String, Object>> fetchSpec;

    private Review review;

    @BeforeEach
    void setup() {
        review = Review.createReview()
                .reviewId(1L)
                .title("바지 리뷰")
                .description("찢어진 바지가 왔네요. 제가 스폰지밥인가요?")
                .ratings(Ratings.ONE)
                .reviewImgList(List.of("바지 리뷰 이미지1", "바지 리뷰 이미지2"))
                .writeDate(LocalDateTime.now())
                .memberId(1L)
                .productId(1L)
                .orderProductId(1L)
                .build();

        databaseClient = mock(DatabaseClient.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        fetchSpec = mock(FetchSpec.class);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);

        reviewRepository = new ReviewRepository(databaseClient);
    }

    @Test
    void save_insert() {
        // given
        review = Review.createReview()
                .reviewId(null)
                .title("바지 리뷰")
                .description("찢어진 바지가 왔네요. 제가 스폰지밥인가요?")
                .ratings(Ratings.ONE)
                .reviewImgList(List.of("바지 리뷰 이미지1", "바지 리뷰 이미지2"))
                .writeDate(LocalDateTime.now())
                .memberId(1L)
                .productId(1L)
                .orderProductId(1L)
                .build();

        // when
        when(executeSpec.bind("title", review.getTitle())).thenReturn(executeSpec);
        when(executeSpec.bind("description", review.getDescription())).thenReturn(executeSpec);
        when(executeSpec.bind("ratings", review.getRatings().getValue())).thenReturn(executeSpec);
        when(executeSpec.bind("reviewImgList", review.getReviewImgList().toArray())).thenReturn(executeSpec);
        when(executeSpec.bind("writeDate", review.getWriteDate())).thenReturn(executeSpec);
        when(executeSpec.bind("memberId", review.getMemberId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", review.getProductId())).thenReturn(executeSpec);
        when(executeSpec.bind("orderProductId", review.getOrderProductId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(reviewRepository.save(review))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(8)).bind(anyString(), any());
    }

    @Test
    void save_update() {
        // given
        // when
        when(executeSpec.bind("title", review.getTitle())).thenReturn(executeSpec);
        when(executeSpec.bind("description", review.getDescription())).thenReturn(executeSpec);
        when(executeSpec.bind("ratings", review.getRatings().getValue())).thenReturn(executeSpec);
        when(executeSpec.bind("reviewImgList", review.getReviewImgList().toArray())).thenReturn(executeSpec);
        when(executeSpec.bind("writeDate", review.getWriteDate())).thenReturn(executeSpec);
        when(executeSpec.bind("memberId", review.getMemberId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", review.getProductId())).thenReturn(executeSpec);
        when(executeSpec.bind("orderProductId", review.getOrderProductId())).thenReturn(executeSpec);
        when(executeSpec.bind("reviewId", review.getReviewId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(reviewRepository.save(review))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(9)).bind(anyString(), any());
    }

    @Test
    void findById() {
        // given
        Map<String, Object> dataMap = Map.of(
                "review_id", review.getReviewId(),
                "title", review.getTitle(),
                "description", review.getDescription(),
                "ratings", review.getRatings().getValue(),
                "review_img_list", review.getReviewImgList().toArray(new String[review.getReviewImgList().size()]),
                "write_date", review.getWriteDate(),
                "member_id", review.getMemberId(),
                "product_id", review.getProductId(),
                "order_product_id", review.getOrderProductId()
        );

        // when
        when(executeSpec.bind("reviewId", review.getReviewId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(reviewRepository.findById(review.getReviewId()))
                .expectNextMatches(result ->
                        result.getReviewId() == review.getReviewId() &&
                                result.getTitle().equals(review.getTitle()) &&
                                result.getDescription().equals(review.getDescription()) &&
                                result.getRatings().getValue() == review.getRatings().getValue() &&
                                result.getReviewImgList().size() == review.getReviewImgList().size() &&
                                result.getWriteDate().isEqual(review.getWriteDate()) &&
                                result.getMemberId() == review.getMemberId() &&
                                result.getProductId() == review.getProductId() &&
                                result.getOrderProductId() == review.getOrderProductId()
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void findAllByProductId() {
        // given
        ReviewDTO.ReviewResp reviewResp = ReviewDTO.ReviewResp.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .description(review.getDescription())
                .ratings(review.getRatings())
                .reviewImgList(review.getReviewImgList())
                .writeDate(review.getWriteDate())
                .memberId(review.getMemberId())
                .writer("tester001")
                .build();

        Map<String, Object> dataMap = Map.of(
                "review_id", review.getReviewId(),
                "title", review.getTitle(),
                "description", review.getDescription(),
                "ratings", review.getRatings().getValue(),
                "review_img_list", review.getReviewImgList().toArray(new String[review.getReviewImgList().size()]),
                "write_date", review.getWriteDate(),
                "member_id", review.getMemberId(),
                "writer", "tester001"
        );

        // when
        when(executeSpec.bind("productId", review.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(dataMap));

        StepVerifier.create(reviewRepository.findAllByProductId(review.getProductId()))
                .expectNextMatches(result ->
                        result.getReviewId() == reviewResp.getReviewId() &&
                                result.getTitle().equals(reviewResp.getTitle()) &&
                                result.getDescription().equals(reviewResp.getDescription()) &&
                                result.getRatings().getValue() == reviewResp.getRatings().getValue() &&
                                result.getReviewImgList().size() == reviewResp.getReviewImgList().size() &&
                                result.getWriteDate().isEqual(reviewResp.getWriteDate()) &&
                                result.getMemberId() == reviewResp.getMemberId() &&
                                result.getWriter().equals(reviewResp.getWriter())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void deleteById() {
        // given
        // when
        when(executeSpec.bind("reviewId", review.getReviewId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(reviewRepository.deleteById(review.getReviewId()))
                .expectNext(1L)
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void reviewCountByProduct() {
        // given
        // when
        when(executeSpec.bind("productId", review.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(Map.of("count", 1L)));

        StepVerifier.create(reviewRepository.reviewCountByProduct(review.getProductId()))
                .expectNext(1L)
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void findByMemberAndOrderProduct() {
        // given
        // when
        when(executeSpec.bind("memberId", review.getMemberId())).thenReturn(executeSpec);
        when(executeSpec.bind("orderProductId", review.getOrderProductId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(Map.of("count", 1L)));

        StepVerifier.create(reviewRepository.findByMemberAndOrderProduct(review.getMemberId(), review.getOrderProductId()))
                .expectNext(1L)
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(2)).bind(anyString(), any());
    }
}