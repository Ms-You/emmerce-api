package commerce.emmerce.repository;

import commerce.emmerce.domain.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class CartRepository {

    private final DatabaseClient databaseClient;

    public Mono<Cart> findByMemberId(Long memberId) {
        String query = """
                select *
                from cart c
                where c.member_id = :memberId
                """;

        return databaseClient.sql(query)
                .bind("memberId", memberId)
                .fetch().one()
                .map(row -> Cart.createCart()
                        .cartId((Long) row.get("cart_id"))
                        .memberId((Long) row.get("member_id"))
                        .build());
    }

}
