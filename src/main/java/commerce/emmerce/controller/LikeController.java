package commerce.emmerce.controller;

import commerce.emmerce.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/product")
@RestController
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{productId}/like")
    public Mono<ResponseEntity> likeProduct(@PathVariable Long productId) {
        return likeService.toggleLike(productId)
                .then(Mono.just(new ResponseEntity(HttpStatus.OK)))
                .onErrorReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));
    }

}
