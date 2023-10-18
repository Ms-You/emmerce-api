package commerce.emmerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CategoryDTO {

    @Getter
    @NoArgsConstructor
    public static class CategoryReq {
        private Integer tier;
        private String name;
        private String code;
        private String parentCode;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryResp {
        private Long categoryId;
        private Integer tier;
        private String name;
        private String code;
        private String parentCode;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryInfoResp {
        private Long categoryId;
        private Integer tier;
        private String name;
    }

}
