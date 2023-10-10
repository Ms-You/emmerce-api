package commerce.emmerce.repository;

import commerce.emmerce.domain.Delivery;
import commerce.emmerce.domain.DeliveryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class DeliveryRepositoryImpl {

    private final DatabaseClient databaseClient;


    public Mono<Void> save(Delivery delivery) {
        String query = """
                insert into delivery (name, tel, email, city, street, zipcode, delivery_status, order_id) 
                values (:name, :tel, :email, :city, :street, :zipcode, :deliveryStatus, :orderId)
                """;

        return databaseClient.sql(query)
                .bind("name", delivery.getName())
                .bind("tel", delivery.getTel())
                .bind("email", delivery.getEmail())
                .bind("city", delivery.getCity())
                .bind("street", delivery.getStreet())
                .bind("zipcode", delivery.getZipcode())
                .bind("deliveryStatus", delivery.getDeliveryStatus())
                .bind("orderId", delivery.getOrderId())
                .then();
    }


    public Mono<Long> updateStatus(Long deliveryId, DeliveryStatus deliveryStatus) {
        String query = """
                update delivery 
                set delivery_status = :deliveryStatus 
                where delivery_id = :deliveryId
                """;

        return databaseClient.sql(query)
                .bind("deliveryStatus", deliveryStatus.getValue())
                .bind("deliveryId", deliveryId)
                .fetch().rowsUpdated();
    }


}
