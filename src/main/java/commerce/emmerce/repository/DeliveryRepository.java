package commerce.emmerce.repository;

import commerce.emmerce.domain.Delivery;
import commerce.emmerce.domain.DeliveryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class DeliveryRepository {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(Delivery delivery) {
        String insertQuery = """
                insert into delivery (name, tel, email, city, street, zipcode, delivery_status, order_product_id)
                values (:name, :tel, :email, :city, :street, :zipcode, :deliveryStatus, :orderProductId)
                """;

        String updateQuery = """
                update delivery
                set name = :name, tel = :tel, email = :email, city = :city, street = :street, zipcode = :zipcode,
                delivery_status = :deliveryStatus, order_product_id = :orderProductId
                where delivery_id = :deliveryId
                """;

        String query = delivery.getDeliveryId() == null ? insertQuery : updateQuery;

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(query)
                .bind("name", delivery.getName())
                .bind("tel", delivery.getTel())
                .bind("email", delivery.getEmail())
                .bind("city", delivery.getCity())
                .bind("street", delivery.getStreet())
                .bind("zipcode", delivery.getZipcode())
                .bind("deliveryStatus", delivery.getDeliveryStatus().name())
                .bind("orderProductId", delivery.getOrderProductId());

        if(delivery.getDeliveryId() != null) {
            executeSpec = executeSpec.bind("deliveryId", delivery.getDeliveryId());
        }

        return executeSpec.then();
    }

    public Mono<Delivery> findByOrderProductId(Long orderProductId) {
        String query = """
                select *
                from delivery
                where order_product_id = :orderProductId
                """;

        return databaseClient.sql(query)
                .bind("orderProductId", orderProductId)
                .fetch().one()
                .map(row -> Delivery.createDelivery()
                        .deliveryId((Long) row.get("delivery_id"))
                        .name((String) row.get("name"))
                        .tel((String) row.get("tel"))
                        .email((String) row.get("email"))
                        .city((String) row.get("city"))
                        .street((String) row.get("street"))
                        .zipcode((String) row.get("zipcode"))
                        .deliveryStatus(DeliveryStatus.valueOf((String) row.get("delivery_status")))
                        .orderProductId((Long) row.get("order_product_id"))
                        .build());
    }

    public Mono<Void> updateStatus(Long deliveryId, Long orderProductId, DeliveryStatus deliveryStatus) {
        String query = """
                update delivery
                set delivery_status = :deliveryStatus
                where delivery_id = :deliveryId
                and
                    order_product_id = :orderProductId
                """;

        return databaseClient.sql(query)
                .bind("deliveryStatus", deliveryStatus.name())
                .bind("deliveryId", deliveryId)
                .bind("orderProductId", orderProductId)
                .then();
    }

}
