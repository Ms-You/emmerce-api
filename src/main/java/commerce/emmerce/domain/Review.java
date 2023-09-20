package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Review {

    @Id
    @GeneratedValue
    @Column(name = "review_id")
    private Long id;

    private String title;

    private String description;

    private Double starScore;   // 별점

    private List<String> reviewImgList;

    @CreatedDate
    private LocalDate writeDate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;


    @Builder(builderMethodName = "createReview")
    private Review(String title, String description, Double startScore, List<String> reviewImgList,
                   LocalDate writeDate, Member member, Product product) {
        this.title = title;
        this.description = description;
        this.starScore = startScore;
        this.reviewImgList = reviewImgList;
        this.writeDate = writeDate;
        this.member = member;
        this.product = product;

        member.getReviewList().add(this);
        product.getReviewList().add(this);
    }


}
