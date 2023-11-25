package commerce.emmerce.controller;

import commerce.emmerce.dto.CartProductDTO;
import commerce.emmerce.service.CartProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "CartProduct", description = "장바구니 상품 관련 컨트롤러")
@RequiredArgsConstructor
@RequestMapping("/cart")
@RestController
public class CartProductController {

    private final CartProductService cartProductService;

    @Operation(summary = "장바구니에 상품 추가", description = "현재 로그인 한 사용자의 장바구니에 상품을 추가합니다.")
    @Parameter(name = "enrollReq", description = "장바구니에 담을 상품과 상품 수량")
    @PostMapping("/product")
    public Mono<Void> putProductInCart(@RequestBody CartProductDTO.EnrollReq enrollReq) {
        return cartProductService.putInCart(enrollReq);
    }


    @Operation(summary = "장바구니에 속한 상품 목록 조회", description = "현재 로그인 한 사용자의 장바구니에 담긴 상품 목록을 조회합니다.")
    @GetMapping("/product")
    public Flux<CartProductDTO.ListResp> cartProductList() {
        return cartProductService.productList();
    }


    @Operation(summary = "장바구니에서 상품 제거", description = "현재 로그인 한 사용자의 장바구니에 담긴 상품 중 특정 상품을 삭제합니다.")
    @Parameter(name = "cartProductId", description = "삭제할 장바구니 상품 id")
    @DeleteMapping("/{cartProductId}")
    public Mono<Void> removeProductInCart(@PathVariable Long cartProductId) {
        return cartProductService.removeInCart(cartProductId);
    }


    @Operation(summary = "장바구니 비우기", description = "현재 로그인 한 사용자의 장바구니에 담긴 상품을 모두 제거합니다.")
    @DeleteMapping("/clear")
    public Mono<Void> clearCart() {
        return cartProductService.clear();
    }

}
