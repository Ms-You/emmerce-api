package commerce.emmerce.controller;

import commerce.emmerce.dto.CartProductDTO;
import commerce.emmerce.service.CartProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/cart")
@RestController
public class CartProductController {

    private final CartProductService cartProductService;

    /**
     * 장바구니에 상품 추가
     * @param cartProductReq
     * @return
     */
    @PostMapping("/product")
    public Mono<ResponseEntity> putProductInCart(@RequestBody CartProductDTO.CartProductReq cartProductReq) {
        return cartProductService.putInCart(cartProductReq)
                .then(Mono.just(new ResponseEntity(HttpStatus.CREATED)))
                .onErrorReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));
    }


    /**
     * 장바구니에 속한 상품 목록 조회
     * @return
     */
    @GetMapping("/product")
    public ResponseEntity<Flux<CartProductDTO.CartProductListResp>> cartProductList() {
        return ResponseEntity.ok().body(cartProductService.productList());
    }


    /**
     * 장바구니에서 상품 제거
     * @param productId
     * @return
     */
    @DeleteMapping("/product/{productId}")
    public Mono<ResponseEntity> removeProductInCart( @PathVariable Long productId) {
        return cartProductService.removeInCart(productId)
                .then(Mono.just(new ResponseEntity(HttpStatus.NO_CONTENT)))
                .onErrorReturn(new ResponseEntity(HttpStatus.NOT_FOUND));
    }


    /**
     * 장바구니 비우기
     * @return
     */
    @DeleteMapping("/clear")
    public Mono<ResponseEntity> clearCart() {
        return cartProductService.clear()
                .then(Mono.just(new ResponseEntity(HttpStatus.NO_CONTENT)))
                .onErrorReturn(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

}
