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
@RequestMapping("/cart/{cartId}")
@RestController
public class CartProductController {

    private final CartProductService cartProductService;

    @PostMapping("/product")
    public Mono<ResponseEntity> putProductInCart(@PathVariable Long cartId, @RequestBody CartProductDTO.CartProductReq cartProductReq) {
        return cartProductService.putInCart(cartId, cartProductReq)
                .then(Mono.just(new ResponseEntity(HttpStatus.CREATED)))
                .onErrorReturn(new ResponseEntity(HttpStatus.BAD_REQUEST));
    }


    @GetMapping("/product")
    public ResponseEntity<Flux<CartProductDTO.CartProductListResp>> cartProductList(@PathVariable Long cartId) {
        return ResponseEntity.ok().body(cartProductService.productList(cartId));
    }


    @DeleteMapping("/product/{productId}")
    public Mono<ResponseEntity> removeProductInCart(@PathVariable Long cartId, @PathVariable Long productId) {
        return cartProductService.removeInCart(cartId, productId)
                .then(Mono.just(new ResponseEntity(HttpStatus.NO_CONTENT)))
                .onErrorReturn(new ResponseEntity(HttpStatus.NOT_FOUND));
    }


    // 장바구니 비우기
    @DeleteMapping("/clear")
    public Mono<ResponseEntity> clearCart(@PathVariable Long cartId) {
        return cartProductService.clear(cartId)
                .then(Mono.just(new ResponseEntity(HttpStatus.NO_CONTENT)))
                .onErrorReturn(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

}
