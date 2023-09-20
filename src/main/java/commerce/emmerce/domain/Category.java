package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Category {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private Integer tier;

    private String name;

    private String code;

    private String parentCode;


    @OneToMany(mappedBy = "category", orphanRemoval = true)
    private List<CategoryProduct> categoryProductList = new ArrayList<>();


    @Builder(builderMethodName = "createCategory")
    private Category(Integer tier, String name, String code, String parentCode) {
        this.tier = tier;
        this.name = name;
        this.code = code;
        this.parentCode = parentCode;
    }

}
