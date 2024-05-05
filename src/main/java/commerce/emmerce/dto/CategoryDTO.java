package commerce.emmerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CategoryDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateReq {
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
    public static class InfoResp {
        private Long categoryId;
        private Integer tier;
        private String name;
    }

}
