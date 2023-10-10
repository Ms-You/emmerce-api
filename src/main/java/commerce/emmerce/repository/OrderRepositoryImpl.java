package commerce.emmerce.repository;

import commerce.emmerce.domain.Order;
import commerce.emmerce.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Repository
public class OrderRepositoryImpl {

    private final DatabaseClient databaseClient;

    public Mono<Order> save(Order order) {
        String query = """
                insert into orders (order_date, order_status, member_id) 
                values (:orderDate, :orderStatus, :memberId)
                returning *
                """;

        return databaseClient.sql(query)
                .bind("orderDate", order.getOrderDate())
                .bind("orderStatus", order.getOrderStatus().name())
                .bind("memberId", order.getMemberId())
                .fetch().one()
                .map(row -> Order.createOrder()
                        .orderId((Long) row.get("order_id"))
                        .orderDate((LocalDateTime) row.get("order_date"))
                        .orderStatus(OrderStatus.valueOf((String) row.get("order_status")))
                        .memberId((Long) row.get("member_id"))
                        .build());
    }

}
