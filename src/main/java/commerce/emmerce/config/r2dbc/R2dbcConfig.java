package commerce.emmerce.config.r2dbc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableR2dbcAuditing
public class R2dbcConfig {
    // R2dbc audit 기능 사용을 위한 클래스

    // enum 타입 사용을 위한 converter 등록
    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Object> converters = new ArrayList<>();
        converters.add(new RoleTypeWriteConverter());
        converters.add(new RoleTypeReadConverter());

        return new R2dbcCustomConversions(CustomConversions.StoreConversions.NONE, converters);
    }
}
