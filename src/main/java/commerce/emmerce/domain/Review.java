package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class Review {

    @Id
    @Column(value = "review_id")
    private Long reviewId;

    private String title;

    private String description;

    private Double starScore;   // 별점

    private List<String> reviewImgList = new ArrayList<>();

    @CreatedDate
    private LocalDate writeDate;

    private Long memberId;

    private Long productId;


    @Builder(builderMethodName = "createReview")
    private Review(Long reviewId, String title, String description, Double startScore,
                   List<String> reviewImgList, LocalDate writeDate, Long memberId, Long productId) {
        this.reviewId = reviewId;
        this.title = title;
        this.description = description;
        this.starScore = startScore;
        this.reviewImgList = reviewImgList;
        this.writeDate = writeDate;
        this.memberId = memberId;
        this.productId = productId;
    }


}
