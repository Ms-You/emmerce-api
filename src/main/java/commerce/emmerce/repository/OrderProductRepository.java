package commerce.emmerce.repository;

import commerce.emmerce.domain.OrderProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class OrderProductRepository {

    private final DatabaseClient databaseClient;

    public Mono<Void> save(OrderProduct orderProduct) {
        String insertQuery = """
                insert into order_product (total_price, total_count, order_id, product_id)
                values (:totalPrice, :totalCount, :orderId, :productId)
                """;

        String updateQuery = """
                update order_product
                set total_price = :totalPrice, total_count = :totalCount,
                    order_id = :orderId, product_id = :productId
                where order_product_id = :orderProductId
                """;

        String query = orderProduct.getOrderProductId() == null ? insertQuery : updateQuery;

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(query)
                .bind("totalPrice", orderProduct.getTotalPrice())
                .bind("totalCount", orderProduct.getTotalCount())
                .bind("orderId", orderProduct.getOrderId())
                .bind("productId", orderProduct.getProductId());

        if(orderProduct.getOrderProductId() != null) {
            executeSpec = executeSpec.bind("orderProductId", orderProduct.getOrderProductId());
        }

        return executeSpec
                .fetch()
                .rowsUpdated()
                .then();
    }

    public Mono<OrderProduct> findById(Long orderProductId) {
        String query = """
                select *
                from order_product op
                where op.order_product_id = :orderProductId
                """;

        return databaseClient.sql(query)
                .bind("orderProductId", orderProductId)
                .fetch().one()
                .map(row -> OrderProduct.builder()
                        .orderProductId((Long) row.get("order_product_id"))
                        .totalPrice((Integer) row.get("total_price"))
                        .totalCount((Integer) row.get("total_count"))
                        .orderId((Long) row.get("order_id"))
                        .productId((Long) row.get("product_id"))
                        .build());
    }

    public Flux<OrderProduct> findAllByOrderId(Long orderId) {
        String query = """
                select *
                from order_product op
                where op.order_id = :orderId
                """;

        return databaseClient.sql(query)
                .bind("orderId", orderId)
                .fetch().all()
                .map(row -> OrderProduct.builder()
                        .orderProductId((Long) row.get("order_product_id"))
                        .totalPrice((Integer) row.get("total_price"))
                        .totalCount((Integer) row.get("total_count"))
                        .orderId((Long) row.get("order_id"))
                        .productId((Long) row.get("product_id"))
                        .build());
    }


}
