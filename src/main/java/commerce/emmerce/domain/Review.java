package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
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

    private Ratings ratings;

    private List<String> reviewImgList = new ArrayList<>();

    @CreatedDate
    private LocalDateTime writeDate;

    private Long memberId;

    private Long productId;

    private Long orderProductId;


    @Builder(builderMethodName = "createReview")
    private Review(Long reviewId, String title, String description, Ratings ratings,
                   List<String> reviewImgList, LocalDateTime writeDate, Long memberId, Long productId, Long orderProductId) {
        this.reviewId = reviewId;
        this.title = title;
        this.description = description;
        this.ratings = ratings;
        this.reviewImgList = reviewImgList;
        this.writeDate = writeDate;
        this.memberId = memberId;
        this.productId = productId;
        this.orderProductId = orderProductId;
    }


}
