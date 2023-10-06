package commerce.emmerce.service;

import commerce.emmerce.domain.CartProduct;
import commerce.emmerce.dto.CartProductDTO;
import commerce.emmerce.repository.CartProductRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class CartProductService {

    private final CartProductRepositoryImpl cartProductRepository;


    public Mono<Void> putInCart(Long cartId, CartProductDTO.CartProductReq cartProductReq) {
        CartProduct cartProduct = CartProduct.builder()
                .cartId(cartId)
                .productId(cartProductReq.getProductId())
                .quantity(cartProductReq.getQuantity())
                .build();

        return cartProductRepository.save(cartProduct);
    }


    public Flux<CartProductDTO.CartProductListResp> productList(Long cartId) {
        // 성능 최적화적인 부분에서 떨어질듯
        /*return cartProductRepository.findAllProductsByCartId(cartId)
                .flatMap(product ->
                        cartProductRepository.findByCartIdAndProductId(cartId, product.getProductId())
                                .map(cartProduct -> CartProductDTO.CartProductListResp.builder()
                                        .productId(product.getProductId())
                                        .name(product.getName())
                                        .titleImgList(product.getTitleImgList())
                                        .discountPrice(product.getDiscountPrice())
                                        .totalCount(cartProduct.getQuantity())
                                        .totalPrice(product.getDiscountPrice() * cartProduct.getQuantity())
                                        .build())
                );*/
        return cartProductRepository.findAllByCartId(cartId);
    }


    public Mono<Void> removeInCart(Long cartId, Long productId) {
        return cartProductRepository.findByCartIdAndProductId(cartId, productId)
                .flatMap(cartProduct ->
                        cartProductRepository.delete(cartProduct)
                );
    }

}
