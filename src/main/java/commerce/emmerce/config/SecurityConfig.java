package commerce.emmerce.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(csrfSpec -> csrfSpec.disable())
                .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))  // cors
                .authorizeExchange((authorizeExchangeSpec ->
                        authorizeExchangeSpec.pathMatchers("/auth/register", "auth/login")
                        .permitAll()
                        .anyExchange()
                        .authenticated()))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())   // session STATELESS
                .addFilterAt(customHeaderOptionFilter(),  SecurityWebFiltersOrder.FIRST)    // XFrameOption custom filter 추가
                .addFilterAt(customAuthenticateAndAccessDeniedHandlingFilter(), SecurityWebFiltersOrder.EXCEPTION_TRANSLATION);   // (authenticatedEntryPoint, accessDenied custom filter 추가
//                .addFilterBefore(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public WebFilter customHeaderOptionFilter() {
        return (exchange, chain) -> {
            exchange.getResponse().getHeaders().set("X-Frame-Options", "SAMEORIGIN");

            return chain.filter(exchange);
        };
    }


    /*
       custom filter 적용하지 않을 때
       .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec.authenticationEntryPoint())
    */
    @Bean
    public WebFilter customAuthenticateAndAccessDeniedHandlingFilter() {
        return (exchange, chain) -> chain.filter(exchange)
                .onErrorResume(AuthenticationException.class, e -> {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                })
                .onErrorResume(AccessDeniedException.class, e -> {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    return response.setComplete();
                });
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addExposedHeader("Authorization");  //브라우저에서 접근가능한 헤더
        configuration.addExposedHeader("RefreshToken");
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true); //Authorization 으로 사용자 인증처리 여부

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }



}
