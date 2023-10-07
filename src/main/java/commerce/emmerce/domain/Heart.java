package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class Heart {

    @Id
    @Column(value = "heart_id")
    private Long heartId;

    private Long memberId;

    private Long productId;


    @Builder(builderMethodName = "createHeart")
    private Heart(Long heartId, Long memberId, Long productId) {
        this.heartId = heartId;
        this.memberId = memberId;
        this.productId = productId;
    }

}
