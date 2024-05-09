package commerce.emmerce.repository;

import commerce.emmerce.domain.Cart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataR2dbcTest
class CartRepositoryTest {
    private CartRepository cartRepository;
    private DatabaseClient databaseClient;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private FetchSpec<Map<String, Object>> fetchSpec;

    @BeforeEach
    void setup() {
        databaseClient = mock(DatabaseClient.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        fetchSpec = mock(FetchSpec.class);

        cartRepository = new CartRepository(databaseClient);
    }

    @Test
    void findByMemberId() {
        // given
        Cart cart = Cart.createCart()
                .cartId(1L)
                .memberId(1L)
                .build();

        Map<String, Object> dataMap = Map.of(
                "cart_id", cart.getCartId(),
                "member_id", cart.getMemberId()
        );

        // when
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind("memberId", cart.getMemberId())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(cartRepository.findByMemberId(cart.getMemberId()))
                .expectNextMatches(result ->
                        result.getCartId() == cart.getCartId() &&
                                result.getMemberId() == cart.getMemberId()
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
    }
}