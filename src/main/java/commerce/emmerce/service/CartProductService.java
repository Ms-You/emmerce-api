package commerce.emmerce.service;

import commerce.emmerce.domain.CartProduct;
import commerce.emmerce.dto.CartProductDTO;
import commerce.emmerce.repository.CartProductRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartProductService {

    private final CartProductRepositoryImpl cartProductRepository;

    /**
     * 장바구니에 상품 추가
     * @param cartId
     * @param cartProductReq
     * @return
     */
    public Mono<Void> putInCart(Long cartId, CartProductDTO.CartProductReq cartProductReq) {
        CartProduct cartProduct = CartProduct.builder()
                .cartId(cartId)
                .productId(cartProductReq.getProductId())
                .quantity(cartProductReq.getQuantity())
                .build();

        return cartProductRepository.save(cartProduct);
    }


    /**
     * 상품 목록 조회
     * @param cartId
     * @return
     */
    public Flux<CartProductDTO.CartProductListResp> productList(Long cartId) {
        return cartProductRepository.findAllByCartId(cartId);
    }


    /**
     * 장바구니에서 상품 제거
     * @param cartId
     * @param productId
     * @return
     */
    public Mono<Void> removeInCart(Long cartId, Long productId) {
        return cartProductRepository.findByCartIdAndProductId(cartId, productId)
                .flatMap(cartProduct ->
                        cartProductRepository.delete(cartProduct)
                );
    }


    /**
     * 장바구니 비우기
     * @param cartId
     * @return
     */
    public Mono<Long> clear(Long cartId) {
        return cartProductRepository.deleteAll(cartId)
                .doOnNext(rowsUpdated ->
                        log.info("삭제된 상품 개수 : {}", rowsUpdated)
                );
    }

}
