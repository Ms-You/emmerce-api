package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table
public class Category {

    @Id
    @Column(name = "category_id")
    private Long id;

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
