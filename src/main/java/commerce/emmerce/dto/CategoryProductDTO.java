package commerce.emmerce.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class CategoryProductDTO {

    @Getter
    @NoArgsConstructor
    public static class CategoryProductReq {
        private Long categoryId;
        private Long productId;
    }


}
