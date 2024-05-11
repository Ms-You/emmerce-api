package commerce.emmerce.repository;

import commerce.emmerce.domain.Order;
import commerce.emmerce.domain.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataR2dbcTest
class OrderRepositoryTest {
    private OrderRepository orderRepository;
    private DatabaseClient databaseClient;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private FetchSpec<Map<String, Object>> fetchSpec;

    private Order order;

    @BeforeEach
    void setup() {
        order = Order.createOrder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETE)
                .memberId(1L)
                .build();

        databaseClient = mock(DatabaseClient.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        fetchSpec = mock(FetchSpec.class);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);

        orderRepository = new OrderRepository(databaseClient);
    }

    @Test
    void save_insert() {
        // given
        order = Order.createOrder()
                .orderId(null)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETE)
                .memberId(1L)
                .build();

        Map<String, Object> dataMap = Map.of(
                "order_id", 1L,
                "order_date", order.getOrderDate(),
                "order_status", order.getOrderStatus().name(),
                "member_id", order.getMemberId()
        );

        // when
        when(executeSpec.bind("orderDate", order.getOrderDate())).thenReturn(executeSpec);
        when(executeSpec.bind("orderStatus", order.getOrderStatus().name())).thenReturn(executeSpec);
        when(executeSpec.bind("memberId", order.getMemberId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(orderRepository.save(order))
                .expectNextMatches(result ->
                        result.getOrderId() == 1L &&
                                result.getOrderDate().isEqual(order.getOrderDate()) &&
                                result.getOrderStatus().equals(order.getOrderStatus()) &&
                                result.getMemberId() == order.getMemberId()
                )
                .verifyComplete();
        
        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(3)).bind(anyString(), any());
    }

    @Test
    void save_update() {
        // given
        Map<String, Object> dataMap = Map.of(
                "order_id", order.getOrderId(),
                "order_date", order.getOrderDate(),
                "order_status", order.getOrderStatus().name(),
                "member_id", order.getMemberId()
        );

        // when
        when(executeSpec.bind("orderDate", order.getOrderDate())).thenReturn(executeSpec);
        when(executeSpec.bind("orderStatus", order.getOrderStatus().name())).thenReturn(executeSpec);
        when(executeSpec.bind("memberId", order.getMemberId())).thenReturn(executeSpec);
        when(executeSpec.bind("orderId", order.getOrderId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(orderRepository.save(order))
                .expectNextMatches(result ->
                        result.getOrderId() == order.getOrderId() &&
                                result.getOrderDate().isEqual(order.getOrderDate()) &&
                                result.getOrderStatus().equals(order.getOrderStatus()) &&
                                result.getMemberId() == order.getMemberId()
                )
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(4)).bind(anyString(), any());
    }

    @Test
    void findByMemberId() {
        // given
        Map<String, Object> dataMap = Map.of(
                "order_id", order.getOrderId(),
                "order_date", order.getOrderDate(),
                "order_status", order.getOrderStatus().name(),
                "member_id", order.getMemberId()
        );

        // when
        when(executeSpec.bind("memberId", order.getMemberId())).thenReturn(executeSpec);
        when(fetchSpec.all()).thenReturn(Flux.just(dataMap));

        StepVerifier.create(orderRepository.findByMemberId(order.getMemberId()))
                .expectNextMatches(result ->
                        result.getOrderId() == order.getOrderId() &&
                                result.getOrderDate().isEqual(order.getOrderDate()) &&
                                result.getOrderStatus().equals(order.getOrderStatus()) &&
                                result.getMemberId() == order.getMemberId()
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void findById() {
        // given
        Map<String, Object> dataMap = Map.of(
                "order_id", order.getOrderId(),
                "order_date", order.getOrderDate(),
                "order_status", order.getOrderStatus().name(),
                "member_id", order.getMemberId()
        );

        // when
        when(executeSpec.bind("orderId", order.getOrderId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(orderRepository.findById(order.getOrderId()))
                .expectNextMatches(result ->
                        result.getOrderId() == order.getOrderId() &&
                                result.getOrderDate().isEqual(order.getOrderDate()) &&
                                result.getOrderStatus().equals(order.getOrderStatus()) &&
                                result.getMemberId() == order.getMemberId()
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void updateStatus() {
        // given
        OrderStatus orderStatus = OrderStatus.CANCEL;

        // when
        when(executeSpec.bind("orderStatus", orderStatus.name())).thenReturn(executeSpec);
        when(executeSpec.bind("orderId", order.getOrderId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(orderRepository.updateStatus(order.getOrderId(), orderStatus))
                .expectNext(1L)
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(2)).bind(anyString(), any());
    }
}