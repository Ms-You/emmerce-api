package commerce.emmerce.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Embeddable
public class Address {

    private String city;

    private String street;

    private String zipcode;

    protected Address() {}

}
