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


//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id")
    private Member member;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "product_id")
    private Product product;


    @Builder(builderMethodName = "createHeart")
    private Heart(Member member, Product product) {
        this.member = member;
        this.product = product;

        member.getHeartList().add(this);
        product.getHeartList().add(this);
    }

}
