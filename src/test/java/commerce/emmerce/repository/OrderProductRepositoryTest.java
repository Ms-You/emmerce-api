package commerce.emmerce.repository;

import commerce.emmerce.domain.OrderProduct;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataR2dbcTest
class OrderProductRepositoryTest {
    private OrderProductRepository orderProductRepository;
    private DatabaseClient databaseClient;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private FetchSpec<Map<String, Object>> fetchSpec;

    private OrderProduct orderProduct;

    @BeforeEach
    void setup() {
        orderProduct = OrderProduct.builder()
                .orderProductId(1L)
                .totalPrice(40000)
                .totalCount(5)
                .orderId(1L)
                .productId(1L)
                .build();

        databaseClient = mock(DatabaseClient.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        fetchSpec = mock(FetchSpec.class);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);

        orderProductRepository = new OrderProductRepository(databaseClient);
    }

    @Test
    void save_insert() {
        // given
        orderProduct = OrderProduct.builder()
                .orderProductId(null)
                .totalPrice(40000)
                .totalCount(5)
                .orderId(1L)
                .productId(1L)
                .build();

        // when
        when(executeSpec.bind("totalPrice", orderProduct.getTotalPrice())).thenReturn(executeSpec);
        when(executeSpec.bind("totalCount", orderProduct.getTotalCount())).thenReturn(executeSpec);
        when(executeSpec.bind("orderId", orderProduct.getOrderId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", orderProduct.getProductId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(orderProductRepository.save(orderProduct))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(4)).bind(anyString(), any());
    }

    @Test
    void save_update() {
        // given
        // when
        when(executeSpec.bind("totalPrice", orderProduct.getTotalPrice())).thenReturn(executeSpec);
        when(executeSpec.bind("totalCount", orderProduct.getTotalCount())).thenReturn(executeSpec);
        when(executeSpec.bind("orderId", orderProduct.getOrderId())).thenReturn(executeSpec);
        when(executeSpec.bind("productId", orderProduct.getProductId())).thenReturn(executeSpec);
        when(executeSpec.bind("orderProductId", orderProduct.getOrderProductId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(orderProductRepository.save(orderProduct))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(5)).bind(anyString(), any());
    }

    @Test
    void findById() {
        // given
        Map<String, Object> dataMap = Map.of(
                "order_product_id", orderProduct.getOrderProductId(),
                "total_price", orderProduct.getTotalPrice(),
                "total_count", orderProduct.getTotalCount(),
                "order_id", orderProduct.getOrderId(),
                "product_id", orderProduct.getProductId()
        );

        // when
        when(executeSpec.bind("orderProductId", orderProduct.getOrderProductId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(orderProductRepository.findById(orderProduct.getOrderProductId()))
                .expectNextMatches(result ->
                        result.getOrderProductId() == orderProduct.getOrderProductId() &&
                                result.getTotalPrice() == orderProduct.getTotalPrice() &&
                                result.getTotalCount() == orderProduct.getTotalCount() &&
                                result.getOrderId() == orderProduct.getOrderId() &&
                                result.getProductId() == orderProduct.getProductId()
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void findAllByOrderId() {
        // given
        Map<String, Object> dataMap = Map.of(
                "order_product_id", orderProduct.getOrderProductId(),
                "total_price", orderProduct.getTotalPrice(),
                "total_count", orderProduct.getTotalCount(),
                "order_id", orderProduct.getOrderId(),
                "product_id", orderProduct.getProductId()
        );

        // when
        when(executeSpec.bind("orderId", orderProduct.getOrderId())).thenReturn(executeSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(dataMap));

        StepVerifier.create(orderProductRepository.findAllByOrderId(orderProduct.getOrderId()))
                .expectNextMatches(result ->
                        result.getOrderProductId() == orderProduct.getOrderProductId() &&
                                result.getTotalPrice() == orderProduct.getTotalPrice() &&
                                result.getTotalCount() == orderProduct.getTotalCount() &&
                                result.getOrderId() == orderProduct.getOrderId() &&
                                result.getProductId() == orderProduct.getProductId()
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

}