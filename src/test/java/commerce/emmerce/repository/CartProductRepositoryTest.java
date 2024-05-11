package commerce.emmerce.repository;

import commerce.emmerce.domain.Cart;
import commerce.emmerce.domain.CartProduct;
import commerce.emmerce.dto.CartProductDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataR2dbcTest
class CartProductRepositoryTest {
    private CartProductRepository cartProductRepository;
    private DatabaseClient databaseClient;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private FetchSpec<Map<String, Object>> fetchSpec;

    private Cart cart;
    private CartProduct cartProduct;

    @BeforeEach
    void setup() {
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

        databaseClient = mock(DatabaseClient.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        fetchSpec = mock(FetchSpec.class);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);

        cartProductRepository = new CartProductRepository(databaseClient);
    }

    @Test
    void save_insert() {
        // given
        cartProduct = CartProduct.builder()
                .cartProductId(null)
                .cartId(cart.getCartId())
                .productId(1L)
                .quantity(10)
                .build();

        // when
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));
        when(executeSpec.bind("cartId", cartProduct.getCartId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", cartProduct.getProductId())).thenReturn(executeSpec);
        when(executeSpec.bind("quantity", cartProduct.getQuantity())).thenReturn(executeSpec);

        StepVerifier.create(cartProductRepository.save(cartProduct))
                .verifyComplete();

        // then
        verify(databaseClient, atLeastOnce()).sql(anyString());
        verify(executeSpec, times(3)).bind(anyString(), any());
    }

    @Test
    void save_update() {
        // given
        // when
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));
        when(executeSpec.bind("cartId", cartProduct.getCartId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", cartProduct.getProductId())).thenReturn(executeSpec);
        when(executeSpec.bind("quantity", cartProduct.getQuantity())).thenReturn(executeSpec);
        when(executeSpec.bind("cartProductId", cartProduct.getCartProductId())).thenReturn(executeSpec);

        StepVerifier.create(cartProductRepository.save(cartProduct))
                .verifyComplete();

        // then
        verify(databaseClient, atLeastOnce()).sql(anyString());
        verify(executeSpec, times(4)).bind(anyString(), any());
    }

    @Test
    void findByCartIdAndCartProductId() {
        // given
        Map<String, Object> dataMap = Map.of(
                "cart_product_id", cartProduct.getCartProductId(),
                "cart_id", cartProduct.getCartId(),
                "product_id", cartProduct.getProductId(),
                "quantity", cartProduct.getQuantity()
        );

        // when
        when(executeSpec.bind("cartId", cartProduct.getCartId())).thenReturn(executeSpec);
        when(executeSpec.bind("cartProductId", cartProduct.getCartProductId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(cartProductRepository.findByCartIdAndCartProductId(cart.getCartId(), cartProduct.getCartProductId()))
                .expectNextMatches(result ->
                        result.getCartProductId() == cartProduct.getCartProductId() &&
                                result.getCartId() == cartProduct.getCartId() &&
                                result.getProductId() == cartProduct.getProductId() &&
                                result.getQuantity() == cartProduct.getQuantity()
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(2)).bind(anyString(), any());
    }

    @Test
    void findByCartIdAndProductId() {
        // given
        Map<String, Object> dataMap = Map.of(
                "cart_product_id", cartProduct.getCartProductId(),
                "cart_id", cartProduct.getCartId(),
                "product_id", cartProduct.getProductId(),
                "quantity", cartProduct.getQuantity()
        );

        // when
        when(executeSpec.bind("cartId", cart.getCartId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", cartProduct.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(cartProductRepository.findByCartIdAndProductId(cart.getCartId(), cartProduct.getProductId()))
                .expectNextMatches(result ->
                        result.getCartProductId() == cartProduct.getCartProductId() &&
                                result.getCartId() == cartProduct.getCartId() &&
                                result.getProductId() == cartProduct.getProductId() &&
                                result.getQuantity() == cartProduct.getQuantity()
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(2)).bind(anyString(), any());
    }

    @Test
    void delete() {
        // given
        // when
        when(executeSpec.bind("cartProductId", cartProduct.getCartProductId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(cartProductRepository.delete(cartProduct))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void deleteAll() {
        // given
        // when
        when(executeSpec.bind("cartId", cart.getCartId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(2L));

        StepVerifier.create(cartProductRepository.deleteAll(cart.getCartId()))
                .expectNextMatches(result -> result == 2L)
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void findAllByCartId() {
        // given
        CartProductDTO.ListResp listResp = CartProductDTO.ListResp.builder()
                .cartProductId(cartProduct.getCartProductId())
                .productId(cartProduct.getProductId())
                .name("샴푸")
                .titleImg("샴푸 이미지")
                .originalPrice(10000)
                .discountPrice(8000)
                .quantity(cartProduct.getQuantity())
                .totalPrice(8000 * cartProduct.getQuantity())
                .brand("에비앙")
                .build();

        Map<String, Object> dataMap = Map.of(
                "cart_product_id", cartProduct.getCartProductId(),
                "product_id", cartProduct.getProductId(),
                "name", "샴푸",
                "title_img", "샴푸 이미지",
                "original_price", 10000,
                "discount_price", 8000,
                "quantity", cartProduct.getQuantity(),
                "total_price", 8000 * cartProduct.getQuantity(),
                "brand", "에비앙"
        );

        // when
        when(executeSpec.bind("cartId", cart.getCartId())).thenReturn(executeSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(dataMap));

        StepVerifier.create(cartProductRepository.findAllByCartId(cart.getCartId()))
                .expectNextMatches(result ->
                        result.getCartProductId() == listResp.getCartProductId() &&
                                result.getProductId() == listResp.getProductId() &&
                                result.getName().equals(listResp.getName()) &&
                                result.getTitleImg().equals(listResp.getTitleImg()) &&
                                result.getOriginalPrice().intValue() == listResp.getOriginalPrice() &&
                                result.getDiscountPrice().intValue() == listResp.getDiscountPrice() &&
                                result.getQuantity() == listResp.getQuantity() &&
                                result.getTotalPrice().intValue() == listResp.getTotalPrice() &&
                                result.getBrand().equals(listResp.getBrand())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }
}