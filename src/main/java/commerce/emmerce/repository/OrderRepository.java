package commerce.emmerce.repository;

import commerce.emmerce.domain.Order;
import commerce.emmerce.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Repository
public class OrderRepository {

    private final DatabaseClient databaseClient;

    public Mono<Order> save(Order order) {
        String insertQuery = """
                insert into orders (order_date, order_status, member_id) 
                values (:orderDate, :orderStatus, :memberId)
                returning *
                """;

        String updateQuery = """
                update orders
                set order_date = :orderDate, order_status = :orderStatus, member_id = :memberId
                where order_id = :orderId
                returning *
                """;

        String query = order.getOrderId() == null ? insertQuery : updateQuery;
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(query)
                .bind("orderDate", order.getOrderDate())
                .bind("orderStatus", order.getOrderStatus().name())
                .bind("memberId", order.getMemberId());

        if(order.getOrderId() != null) {
            executeSpec = executeSpec.bind("orderId", order.getOrderId());
        }

        return executeSpec.fetch().one()
                .map(row -> Order.createOrder()
                        .orderId((Long) row.get("order_id"))
                        .orderDate((LocalDateTime) row.get("order_date"))
                        .orderStatus(OrderStatus.valueOf((String) row.get("order_status")))
                        .memberId((Long) row.get("member_id"))
                        .build());
    }

    public Flux<Order> findByMemberId(Long memberId) {
        String query = """
                select *
                from orders o
                where o.member_id = :memberId
                order by o.order_date desc
                """;

        return databaseClient.sql(query)
                .bind("memberId", memberId)
                .fetch().all()
                .map(row -> Order.createOrder()
                        .orderId((Long) row.get("order_id"))
                        .orderDate((LocalDateTime) row.get("order_date"))
                        .orderStatus(OrderStatus.valueOf((String) row.get("order_status")))
                        .memberId((Long) row.get("member_id"))
                        .build());
    }

    public Mono<Order> findById(Long orderId) {
        String query = """
                select *
                from orders o
                where o.order_id = :orderId
                """;

        return databaseClient.sql(query)
                .bind("orderId", orderId)
                .fetch().one()
                .map(row -> Order.createOrder()
                        .orderId((Long) row.get("order_id"))
                        .orderDate((LocalDateTime) row.get("order_date"))
                        .orderStatus(OrderStatus.valueOf((String) row.get("order_status")))
                        .memberId((Long) row.get("member_id"))
                        .build());
    }

    public Mono<Long> updateStatus(Long orderId, OrderStatus orderStatus) {
        String query = """
                update orders
                set order_status = :orderStatus
                where order_id = :orderId
                """;

        return databaseClient.sql(query)
                .bind("orderStatus", orderStatus.name())
                .bind("orderId", orderId)
                .fetch().rowsUpdated();
    }

}
