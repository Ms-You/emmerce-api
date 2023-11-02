package commerce.emmerce.controller;

import commerce.emmerce.dto.ReviewDTO;
import commerce.emmerce.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "Review", description = "리뷰 관련 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/review")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "상품을 주문하고 배송이 완료된 사용자에 한해 상품에 대해 리뷰를 작성합니다.")
    @Parameter(name = "reviewReq", description = "주문 정보, 상품 정보 및 작성할 리뷰 정보")
    @PostMapping
    public Mono<Void> writeReview(@RequestBody ReviewDTO.ReviewReq reviewReq) {
        return reviewService.write(reviewReq);
    }


    @Operation(summary = "리뷰 제거", description = "작성한 리뷰를 삭제합니다.")
    @Parameter(name = "reviewId", description = "삭제할 리뷰 id")
    @DeleteMapping("/{reviewId}")
    public Mono<Void> removeReview(@PathVariable Long reviewId) {
        return reviewService.remove(reviewId);
    }

}
