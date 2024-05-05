package commerce.emmerce.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import commerce.emmerce.config.exception.GlobalErrorAttributes;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.domain.Ratings;
import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.ReviewDTO;
import commerce.emmerce.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser
@WebFluxTest(ReviewController.class)
class ReviewControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    @DisplayName("리뷰 작성 테스트")
    void writeReview() throws JsonProcessingException {
        // given
        ReviewDTO.ReviewReq reviewReq = new ReviewDTO.ReviewReq("샴푸 리뷰", "머리 안빠지는거 맞아요?", Ratings.ONE, 1L);

        ObjectMapper objectMapper = new ObjectMapper();
        String reviewReqJson = objectMapper.writeValueAsString(reviewReq);

        // MultipartBodyBuilder - 리액티브 타입을 지원하지 않음
        // 따라서 ReviewReq 객체를 JSON 문자열로 직렬화해서 추가함
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("reviewReq", reviewReqJson, MediaType.APPLICATION_JSON);

        Resource resource = new ByteArrayResource("bytes" .getBytes()) {
            @Override
            public String getFilename() {
                return "테스트.jpg";
            }
        };

        bodyBuilder.part("reviewImages", resource);

        // when
        when(reviewService.write(any(Mono.class), any(Flux.class))).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/review")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(bodyBuilder.build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(reviewService).write(any(Mono.class), any(Flux.class));
    }

    @Test
    @DisplayName("리뷰 삭제 테스트")
    void removeReview() {
        // given

        // when
        when(reviewService.remove(anyLong())).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf())
                .delete()
                .uri("/review/{reviewId}", 1L)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(reviewService).remove(anyLong());
    }

    @Test
    @DisplayName("리뷰 목록 조회 테스트")
    void getReviews() {
        // given
        ReviewDTO.ReviewResp reviewResp1 = ReviewDTO.ReviewResp.builder()
                .reviewId(1L)
                .title("샴푸 리뷰")
                .description("머리가 안자라는데요?")
                .ratings(Ratings.ONE)
                .reviewImgList(List.of("샴푸 리뷰 이미지1", "샴푸 리뷰 이미지2"))
                .writeDate(LocalDateTime.now())
                .memberId(1L)
                .writer("작성자1")
                .build();

        ReviewDTO.ReviewResp reviewResp2 = ReviewDTO.ReviewResp.builder()
                .reviewId(1L)
                .title("린스 리뷰")
                .description("머리가 찰랑찰랑")
                .ratings(Ratings.FIVE)
                .reviewImgList(List.of("린스 리뷰 이미지1", "린스 리뷰 이미지2"))
                .writeDate(LocalDateTime.now())
                .memberId(2L)
                .writer("작성자2")
                .build();

        PageResponseDTO<ReviewDTO.ReviewResp> pageResponseDTO = new PageResponseDTO<>(List.of(reviewResp1, reviewResp2), 1, 2, 2);

        // when
        when(reviewService.reviewsByProduct(anyLong(), anyInt(), anyInt())).thenReturn(Mono.just(pageResponseDTO));

        webTestClient
                .get()
                .uri("/product/{productId}/reviews", 1L)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.[0].reviewId").isEqualTo(reviewResp1.getReviewId())
                .jsonPath("$.content.[0].title").isEqualTo(reviewResp1.getTitle())
                .jsonPath("$.content.[0].description").isEqualTo(reviewResp1.getDescription())
                .jsonPath("$.content.[0].ratings").isEqualTo(reviewResp1.getRatings().getValue())
                .jsonPath("$.content.[0].reviewImgList").isArray()
                .jsonPath("$.content.[0].writeDate").exists()
                .jsonPath("$.content.[0].memberId").isEqualTo(reviewResp1.getMemberId())
                .jsonPath("$.content.[0].writer").isEqualTo(reviewResp1.getWriter())
                .jsonPath("$.content.[1].reviewId").isEqualTo(reviewResp2.getReviewId())
                .jsonPath("$.content.[1].title").isEqualTo(reviewResp2.getTitle())
                .jsonPath("$.content.[1].description").isEqualTo(reviewResp2.getDescription())
                .jsonPath("$.content.[1].ratings").isEqualTo(reviewResp2.getRatings().getValue())
                .jsonPath("$.content.[1].reviewImgList").isArray()
                .jsonPath("$.content.[1].writeDate").exists()
                .jsonPath("$.content.[1].memberId").isEqualTo(reviewResp2.getMemberId())
                .jsonPath("$.content.[1].writer").isEqualTo(reviewResp2.getWriter())
                .jsonPath("$.pageNumber").isEqualTo(pageResponseDTO.getPageNumber())
                .jsonPath("$.totalPages").isEqualTo(pageResponseDTO.getTotalPages())
                .jsonPath("$.totalElements").isEqualTo(pageResponseDTO.getTotalElements())
                .jsonPath("$.first").isEqualTo(pageResponseDTO.isFirst())
                .jsonPath("$.last").isEqualTo(pageResponseDTO.isLast());

        // then
        verify(reviewService).reviewsByProduct(anyLong(), anyInt(), anyInt());
    }
}