package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class Cart {

    @Id
    @Column(name = "cart_id")
    private Long id;

    private Long memberId;

    @Builder(builderMethodName = "createCart")
    private Cart(Long memberId) {
        this.memberId = memberId;
    }

}
