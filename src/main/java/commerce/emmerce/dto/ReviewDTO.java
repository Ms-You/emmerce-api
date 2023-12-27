package commerce.emmerce.dto;

import commerce.emmerce.domain.Ratings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class ReviewDTO {

    @Getter
    @NoArgsConstructor
    public static class ReviewReq {
        private String title;
        private String description;
        private Ratings ratings;
        private Long orderId;
        private Long productId;
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewResp {
        private Long reviewId;
        private String title;
        private String description;
        private Ratings ratings;
        private List<String> reviewImgList;
        private LocalDate writeDate;
        private Long memberId;
        private String writer;
    }

}
