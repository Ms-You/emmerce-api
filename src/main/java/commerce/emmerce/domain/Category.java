package commerce.emmerce.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table
public class Category {

    @Id
    @Column(value = "category_id")
    private Long categoryId;

    private Integer tier;

    private String name;

    private String code;

    private String parentCode;


    @Builder(builderMethodName = "createCategory")
    private Category(Integer tier, String name, String code, String parentCode) {
        this.tier = tier;
        this.name = name;
        this.code = code;
        this.parentCode = parentCode;
    }

}
