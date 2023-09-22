package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class Heart {

    @Id
    @Column(name = "heart_id")
    private Long id;

    private Long memberId;

    private Long productId;


    @Builder(builderMethodName = "createHeart")
    private Heart(Long memberId, Long productId) {
        this.memberId = memberId;
        this.productId = productId;
    }

}
