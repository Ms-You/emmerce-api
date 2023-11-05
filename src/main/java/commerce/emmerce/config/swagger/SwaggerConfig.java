package commerce.emmerce.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("이머스 프로젝트 API Document")
                .version("v0.0.1")
                .description("이머스 프로젝트의 API 명세서입니다.");

        /**
         * 기본적으로 OpenAPI 3에서는 API 호출 시 인증 처리를 위해 Security Scheme 을 지원
         */
        // SecuritySchemes 명
        String jwtSchemeName = "jwtAuth";
        // API 요청 헤더에 인증 정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // SecuritySchemes 등록
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components)
                .info(info);
    }

    @Bean
    public GroupedOpenApi adminOpenApi() {
        return GroupedOpenApi.builder()
                .group("Admin")
                .pathsToMatch("/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi categoryOpenApi() {
        return GroupedOpenApi.builder()
                .group("Category")
                .pathsToMatch("/category/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cartOpenApi() {
        return GroupedOpenApi.builder()
                .group("Cart")
                .pathsToMatch("/cart/**")
                .build();
    }

    @Bean
    public GroupedOpenApi orderOpenApi() {
        return GroupedOpenApi.builder()
                .group("Order")
                .pathsToMatch("/order/**")
                .build();
    }

    @Bean
    public GroupedOpenApi paymentOpenApi() {
        return GroupedOpenApi.builder()
                .group("Payment")
                .pathsToMatch("/payment/**")
                .build();
    }

    @Bean
    public GroupedOpenApi productOpenApi() {
        return GroupedOpenApi.builder()
                .group("Product")
                .pathsToMatch("/product/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reviewOpenApi() {
        return GroupedOpenApi.builder()
                .group("Review")
                .pathsToMatch("/review/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authOpenApi() {
        return GroupedOpenApi.builder()
                .group("Auth")
                .pathsToMatch("/auth/**")
                .build();
    }

}
