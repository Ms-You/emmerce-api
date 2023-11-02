package commerce.emmerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchParamDTO {

    private String keyword;
    private String brand;
    private Integer limit;
    private Integer minPrice;
    private Integer maxPrice;
    private Integer page;
    private Integer size;
}
