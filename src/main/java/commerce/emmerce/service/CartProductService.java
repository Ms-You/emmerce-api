package commerce.emmerce.service;

import commerce.emmerce.config.SecurityUtil;
import commerce.emmerce.domain.Cart;
import commerce.emmerce.domain.CartProduct;
import commerce.emmerce.domain.Member;
import commerce.emmerce.dto.CartProductDTO;
import commerce.emmerce.repository.CartProductRepository;
import commerce.emmerce.repository.CartRepository;
import commerce.emmerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class CartProductService {

    private final CartProductRepository cartProductRepository;
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;

    /**
     * 장바구니에 상품 추가
     * @param enrollReq
     * @return
     */
    @Transactional
    public Mono<Void> putInCart(CartProductDTO.EnrollReq enrollReq) {
        return getCurrentMemberCart()
                .flatMap(cart -> cartProductRepository.save(CartProduct.builder()
                        .cartId(cart.getCartId())
                        .productId(enrollReq.getProductId())
                        .quantity(enrollReq.getQuantity())
                        .build())
                );
    }

    /**
     * 상품 목록 조회
     * @return
     */
    public Flux<CartProductDTO.ListResp> productList() {
        return getCurrentMemberCart()
                .flatMapMany(cart -> cartProductRepository.findAllByCartId(cart.getCartId()));
    }

    /**
     * 장바구니에서 상품 제거
     * @param productId
     * @return
     */
    @Transactional
    public Mono<Void> removeInCart(Long productId) {
        return getCurrentMemberCart()
                .flatMap(cart -> cartProductRepository.findByCartIdAndProductId(cart.getCartId(), productId)
                        .flatMap(cartProduct -> cartProductRepository.delete(cartProduct))
                );
    }

    /**
     * 장바구니 비우기
     * @return
     */
    @Transactional
    public Mono<Void> clear() {
        return getCurrentMemberCart()
                .flatMap(cart -> cartProductRepository.deleteAll(cart.getCartId())
                        .doOnNext(rowsUpdated -> log.info("삭제된 상품 개수: {}", rowsUpdated))
                ).then();
    }

    /**
     * 현재 로그인 한 사용자 정보 반환
     * @return
     */
    private Mono<Member> findCurrentMember() {
        return SecurityUtil.getCurrentMemberName()
                .flatMap(name -> memberRepository.findByName(name));
    }

    /**
     * 장바구니 반환
     * @return
     */
    private Mono<Cart> getCurrentMemberCart() {
        return findCurrentMember()
                .flatMap(member -> cartRepository.findByMemberId(member.getMemberId()));
    }

}
