package commerce.emmerce.controller;

import commerce.emmerce.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Product", description = "상품 관련 컨트롤러")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/product")
@RestController
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "상품 좋아요 (토글)", description = "특정 상품 좋아요 정보를 토글로 추가 및 삭제")
    @Parameter(name = "productId", description = "좋아요를 누를 상품 id")
    @PostMapping("/{productId}/like")
    public Mono<Void> likeProduct(@PathVariable Long productId) {
        return likeService.toggleLike(productId);
    }

}
