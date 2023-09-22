package commerce.emmerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@Configuration
@EnableR2dbcAuditing
public class R2dbcConfig {
    // R2dbc audit 기능 사용을 위한 클래스
}
