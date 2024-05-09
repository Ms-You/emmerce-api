package commerce.emmerce.repository;

import commerce.emmerce.domain.Delivery;
import commerce.emmerce.domain.DeliveryStatus;
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
class DeliveryRepositoryTest {
    private DeliveryRepository deliveryRepository;
    private DatabaseClient databaseClient;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private FetchSpec<Map<String, Object>> fetchSpec;

    private Delivery delivery;

    @BeforeEach
    void setup() {
        delivery = Delivery.createDelivery()
                .deliveryId(1L)
                .name("tester001")
                .tel("01012345678")
                .email("test@test.com")
                .city("서울특별시")
                .street("공룡로 50")
                .zipcode("18888")
                .deliveryStatus(DeliveryStatus.READY)
                .orderProductId(1L)
                .build();

        databaseClient = mock(DatabaseClient.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        fetchSpec = mock(FetchSpec.class);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);

        deliveryRepository = new DeliveryRepository(databaseClient);
    }

    @Test
    void save_insert() {
        // given
        delivery = Delivery.createDelivery()
                .deliveryId(null)
                .name("tester001")
                .tel("01012345678")
                .email("test@test.com")
                .city("서울특별시")
                .street("공룡로 50")
                .zipcode("18888")
                .deliveryStatus(DeliveryStatus.READY)
                .orderProductId(1L)
                .build();

        // when
        when(executeSpec.bind("name", delivery.getName())).thenReturn(executeSpec);
        when(executeSpec.bind("tel", delivery.getTel())).thenReturn(executeSpec);
        when(executeSpec.bind("email", delivery.getEmail())).thenReturn(executeSpec);
        when(executeSpec.bind("city", delivery.getCity())).thenReturn(executeSpec);
        when(executeSpec.bind("street", delivery.getStreet())).thenReturn(executeSpec);
        when(executeSpec.bind("zipcode", delivery.getZipcode())).thenReturn(executeSpec);
        when(executeSpec.bind("deliveryStatus", delivery.getDeliveryStatus().name())).thenReturn(executeSpec);
        when(executeSpec.bind("orderProductId", delivery.getOrderProductId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(deliveryRepository.save(delivery))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(8)).bind(anyString(), any());
    }

    @Test
    void save_update() {
        // given
        // when
        when(executeSpec.bind("name", delivery.getName())).thenReturn(executeSpec);
        when(executeSpec.bind("tel", delivery.getTel())).thenReturn(executeSpec);
        when(executeSpec.bind("email", delivery.getEmail())).thenReturn(executeSpec);
        when(executeSpec.bind("city", delivery.getCity())).thenReturn(executeSpec);
        when(executeSpec.bind("street", delivery.getStreet())).thenReturn(executeSpec);
        when(executeSpec.bind("zipcode", delivery.getZipcode())).thenReturn(executeSpec);
        when(executeSpec.bind("deliveryStatus", delivery.getDeliveryStatus().name())).thenReturn(executeSpec);
        when(executeSpec.bind("orderProductId", delivery.getOrderProductId())).thenReturn(executeSpec);
        when(executeSpec.bind("deliveryId", delivery.getDeliveryId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(deliveryRepository.save(delivery))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(9)).bind(anyString(), any());
    }

    @Test
    void findByOrderProductId() {
        // given
        Map<String, Object> dataMap = Map.of(
                "delivery_id", delivery.getDeliveryId(),
                "name", delivery.getName(),
                "tel", delivery.getTel(),
                "email", delivery.getEmail(),
                "city", delivery.getCity(),
                "street", delivery.getStreet(),
                "zipcode", delivery.getZipcode(),
                "delivery_status", "READY",
                "order_product_id", delivery.getOrderProductId()
        );

        // when
        when(executeSpec.bind("orderProductId", delivery.getOrderProductId())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(deliveryRepository.findByOrderProductId(delivery.getOrderProductId()))
                .expectNextMatches(result ->
                        result.getDeliveryId() == delivery.getDeliveryId() &&
                                result.getName().equals(delivery.getName()) &&
                                result.getTel().equals(delivery.getTel()) &&
                                result.getEmail().equals(delivery.getEmail()) &&
                                result.getCity().equals(delivery.getCity()) &&
                                result.getStreet().equals(delivery.getStreet()) &&
                                result.getZipcode().equals(delivery.getZipcode()) &&
                                result.getDeliveryStatus().equals(delivery.getDeliveryStatus()) &&
                                result.getOrderProductId() == delivery.getOrderProductId()
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void updateStatus() {
        // given
        DeliveryStatus deliveryStatus = DeliveryStatus.COMPLETE;

        // when
        when(executeSpec.bind("deliveryStatus", deliveryStatus.name())).thenReturn(executeSpec);
        when(executeSpec.bind("deliveryId", delivery.getDeliveryId())).thenReturn(executeSpec);
        when(executeSpec.bind("orderProductId", delivery.getOrderProductId())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(deliveryRepository.updateStatus(delivery.getDeliveryId(), delivery.getOrderProductId(), deliveryStatus))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(3)).bind(anyString(), any());
    }
}