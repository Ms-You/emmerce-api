package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("likes")
public class Like {

    @Id
    @Column(value = "like_id")
    private Long likeId;

    private Long memberId;

    private Long productId;


    @Builder
    private Like(Long likeId, Long memberId, Long productId) {
        this.likeId = likeId;
        this.memberId = memberId;
        this.productId = productId;
    }

}
