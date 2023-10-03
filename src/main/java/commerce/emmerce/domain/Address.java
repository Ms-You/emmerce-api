package commerce.emmerce.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Address {  // r2dbc 는 embeddable 을 지원하지 않음

    private String city;

    private String street;

    private String zipcode;

    protected Address() {}

}
