package commerce.emmerce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReviewDTO {

    @Getter
    @NoArgsConstructor
    public static class ReviewReq {
        private String title;
        private String description;
        private Double starScore;
        private List<String> reviewImgList;
        private Long orderId;
        private Long productId;
    }

}
