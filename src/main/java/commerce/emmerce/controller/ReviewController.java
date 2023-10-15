package commerce.emmerce.controller;

import commerce.emmerce.dto.ReviewDTO;
import commerce.emmerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/review")
@RestController
public class ReviewController {

    private final ReviewService reviewService;


    @PostMapping
    public Mono<ResponseEntity> writeReview(@RequestBody ReviewDTO.ReviewReq reviewReq) {
        return reviewService.write(reviewReq)
                .then(Mono.just(new ResponseEntity(HttpStatus.CREATED)))
                .onErrorReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));
    }


    @DeleteMapping("/{reviewId}")
    public Mono<ResponseEntity> removeReview(@PathVariable Long reviewId) {
        return reviewService.remove(reviewId)
                .then(Mono.just(new ResponseEntity(HttpStatus.NO_CONTENT)))
                .onErrorReturn(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

}
