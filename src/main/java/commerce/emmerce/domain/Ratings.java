package commerce.emmerce.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Ratings {

    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5);

    private int value;

    Ratings(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static Ratings forValue(int value) {
        for(Ratings ratings : Ratings.values()) {
            if(ratings.getValue() == value) {
                return ratings;
            }
        }

        throw new IllegalArgumentException("올바르지 않은 Ratings value 입니다. - " + value);
    }

}
