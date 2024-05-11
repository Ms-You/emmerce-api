package commerce.emmerce.service;

import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.domain.Cart;
import commerce.emmerce.domain.CartProduct;
import commerce.emmerce.domain.Member;
import commerce.emmerce.domain.RoleType;
import commerce.emmerce.dto.CartProductDTO;
import commerce.emmerce.repository.CartProductRepository;
import commerce.emmerce.repository.CartRepository;
import commerce.emmerce.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(CartProductService.class)
class CartProductServiceTest {
    @Autowired
    private CartProductService cartProductService;

    @MockBean
    private CartProductRepository cartProductRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private MemberRepository memberRepository;

    private Member member;
    private Cart cart;
    private CartProduct cartProduct;
    private SecurityContext securityContext;

    @BeforeEach
    void setup() {
        member = Member.createMember()
                .id(1L)
                .name("testId001")
                .email("test@test.com")
                .password("password")
                .tel("01012345678")
                .birth("240422")
                .point(0)
                .role(RoleType.ROLE_USER)
                .city("서울특별시")
                .street("공룡로 50")
                .zipcode("18888")
                .build();

        cart = Cart.createCart()
                .cartId(1L)
                .memberId(1L)
                .build();

        cartProduct = CartProduct.builder()
                .cartProductId(1L)
                .cartId(cart.getCartId())
                .productId(1L)
                .quantity(10)
                .build();

        when(memberRepository.findByName(member.getName())).thenReturn(Mono.just(member));
        when(cartRepository.findByMemberId(member.getMemberId())).thenReturn(Mono.just(cart));

        Authentication authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(member.getName());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(securityContext.getAuthentication().getName()).thenReturn(member.getName());
    }

    @Test
    @DisplayName("장바구니에 상품 추가 테스트")
    void putInCart() {
        // given
        CartProductDTO.EnrollReq enrollReq = new CartProductDTO.EnrollReq(1L, 10);

        // when
        when(cartProductRepository.findByCartIdAndProductId(cart.getCartId(), enrollReq.getProductId())).thenReturn(Mono.just(cartProduct));
        when(cartProductRepository.save(any(CartProduct.class))).thenReturn(Mono.empty());

        StepVerifier.create(cartProductService.putInCart(enrollReq)
                // 리액티브 스트림이기 때문에 아래와 같이 반환하는 코드 추가
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .verifyComplete();

        // then
        verify(cartProductRepository, times(1)).findByCartIdAndProductId(cart.getCartId(), enrollReq.getProductId());
        verify(cartProductRepository, times(1)).save(any(CartProduct.class));
    }

    @Test
    @DisplayName("장바구니에 속한 상품 목록 조회 테스트")
    void productList() {
        // given
        CartProductDTO.ListResp listResp = CartProductDTO.ListResp.builder()
                .cartProductId(cartProduct.getCartProductId())
                .productId(cartProduct.getProductId())
                .name("샴푸")
                .titleImg("샴푸 이미지")
                .originalPrice(10000)
                .discountPrice(8000)
                .quantity(3)
                .totalPrice(24000)
                .brand("에비앙")
                .build();

        // when
        when(cartProductRepository.findAllByCartId(cart.getCartId())).thenReturn(Flux.just(listResp));

        StepVerifier.create(cartProductService.productList()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectNextMatches(result ->
                        result.getProductId() == cartProduct.getProductId() &&
                                result.getCartProductId() == cartProduct.getCartProductId() &&
                                result.getName().equals("샴푸") &&
                                result.getTitleImg().equals("샴푸 이미지") &&
                                result.getOriginalPrice() == 10000 &&
                                result.getDiscountPrice() == 8000 &&
                                result.getQuantity() == 3 &&
                                result.getTotalPrice() == 24000 &&
                                result.getBrand().equals("에비앙")
                )
                .verifyComplete();

        // then
        verify(cartProductRepository, times(1)).findAllByCartId(cart.getCartId());
    }

    @Test
    @DisplayName("장바구니에 속한 상품 삭제 테스트 - 성공")
    void removeInCart_success() {
        // given
        // when
        when(cartProductRepository.findByCartIdAndCartProductId(cart.getCartId(), cartProduct.getCartProductId())).thenReturn(Mono.just(cartProduct));
        when(cartProductRepository.delete(cartProduct)).thenReturn(Mono.empty());

        StepVerifier.create(cartProductService.removeInCart(cartProduct.getCartProductId())
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        ).verifyComplete();

        // then
        verify(cartProductRepository, times(1)).findByCartIdAndCartProductId(cart.getCartId(), cartProduct.getCartProductId());
        verify(cartProductRepository, times(1)).delete(cartProduct);
    }

    @Test
    @DisplayName("장바구니에 속한 상품 삭제 테스트 - 실패")
    void removeInCart_failure() {
        // given
        // when
        when(cartProductRepository.findByCartIdAndCartProductId(cart.getCartId(), cartProduct.getCartProductId())).thenReturn(Mono.empty());

        StepVerifier.create(cartProductService.removeInCart(cartProduct.getCartProductId())
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                ).expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.CART_PRODUCT_NOT_FOUND)
                .verify();

        // then
        verify(cartProductRepository, times(1)).findByCartIdAndCartProductId(cart.getCartId(), cartProduct.getCartProductId());
    }

    @Test
    @DisplayName("장바구니 비우기 테스트")
    void clear() {
        // given
        // when
        when(cartProductRepository.deleteAll(cart.getCartId())).thenReturn(Mono.just(10L));

        StepVerifier.create(cartProductService.clear()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        ).verifyComplete();

        // then
        verify(cartProductRepository, times(1)).deleteAll(cart.getCartId());
    }
}