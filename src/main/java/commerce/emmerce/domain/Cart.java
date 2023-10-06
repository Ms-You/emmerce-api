package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class Cart {

    @Id
    @Column(value = "cart_id")
    private Long cartId;

    private Long memberId;

    @Builder(builderMethodName = "createCart")
    private Cart(Long cartId, Long memberId) {
        this.cartId = cartId;
        this.memberId = memberId;
    }

}
