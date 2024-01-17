package commerce.emmerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchParamDTO {

    private String keyword;
    private String brand;
    private Integer minPrice;
    private Integer maxPrice;
    private String sort;
    private Integer page;
    private Integer size;
}
