package commerce.emmerce.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    private String parent_code;


    @OneToMany(mappedBy = "category", orphanRemoval = true)
    private List<CategoryProduct> category_product_list = new ArrayList<>();

}
