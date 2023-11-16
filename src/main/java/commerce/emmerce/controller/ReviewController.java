package commerce.emmerce.controller;

import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.ReviewDTO;
import commerce.emmerce.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Review", description = "리뷰 관련 컨트롤러")
@RequiredArgsConstructor
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "상품을 주문하고 배송이 완료된 사용자에 한해 상품에 대해 리뷰를 작성합니다.")
    @Parameters({ @Parameter(name = "reviewReq", description = "주문 정보, 상품 정보 및 작성할 리뷰 정보를 전달"),
                @Parameter(name = "reviewImages", description = "리뷰에 포함할 이미지 목록을 전달")})
    @PostMapping("/review")
    public Mono<Void> writeReview(@RequestPart("reviewReq") Mono<ReviewDTO.ReviewReq> reviewReqMono,
                                  @RequestPart("reviewImages") Flux<FilePart> reviewImages) {
        return reviewService.write(reviewReqMono, reviewImages);
    }


    @Operation(summary = "리뷰 삭제", description = "작성한 리뷰를 삭제합니다.")
    @Parameter(name = "reviewId", description = "삭제할 리뷰 id")
    @DeleteMapping("/review/{reviewId}")
    public Mono<Void> removeReview(@PathVariable Long reviewId) {
        return reviewService.remove(reviewId);
    }


    @Operation(summary = "리뷰 목록 조회", description = "상품에 속한 리뷰 목록을 페이징으로 조회합니다.")
    @Parameters({ @Parameter(name = "productId", description = "조회할 상품 id"),
                @Parameter(name = "page", description = "페이지 번호 (기본 값: 1)"),
                @Parameter(name = "size", description = "한 페이지에 조회할 리뷰 수 (기본 값: 10)") })
    @GetMapping("/product/{productId}/reviews")
    public Mono<PageResponseDTO<ReviewDTO.ReviewResp>> getReviews(@PathVariable Long productId,
                                                                  @RequestParam(defaultValue = "1") Integer page,
                                                                  @RequestParam(defaultValue = "10") Integer size) {
        page = Math.max(1, page);
        return reviewService.reviewsByProduct(productId, page, size);
    }

}
