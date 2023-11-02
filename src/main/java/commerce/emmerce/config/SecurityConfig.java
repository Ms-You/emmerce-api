package commerce.emmerce.config;

import commerce.emmerce.config.jwt.JwtAuthenticationFilter;
import commerce.emmerce.domain.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomReactiveUserDetailsService userDetailsService;

    private static final String[] AUTH_WHITE_LIST = {
            "/auth/**",
            "/swagger/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/v3/api-docs/**",
            "/v3/**",
            "/category/**",
            "/product/**"
    };

    private static final String[] AUTH_ADMIN_LIST = {
            // 관리자 권한
            "/admin/**"
    };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(csrfSpec -> csrfSpec.disable())
                .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))  // cors
                .authorizeExchange((authorizeExchangeSpec ->
                        authorizeExchangeSpec.pathMatchers(AUTH_WHITE_LIST)
                                .permitAll()
                                .pathMatchers(AUTH_ADMIN_LIST)
                                .hasAuthority(RoleType.ROLE_ADMIN.toString())
                                .anyExchange()
                                .authenticated()))
                .authenticationManager(authenticationManager())
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())   // session STATELESS
                .exceptionHandling(exceptionHandlingSpec ->
                        exceptionHandlingSpec.authenticationEntryPoint(((exchange, ex) -> {
                            return Mono.fromRunnable(() ->
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)
                            );
                        })).accessDeniedHandler(((exchange, denied) -> {
                            return Mono.fromRunnable(() ->
                                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)
                            );
                        }))
                )
                .headers(headerSpec ->
                        headerSpec.frameOptions(frameOptionsSpec ->
                                frameOptionsSpec.mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN)))
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.HTTP_BASIC);

        return http.build();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * AuthenticationManager는 authentication() 이라는 메서드를 가지고 있는데
     * 필터를 통해 넘어온 Authentication 객체가 존재하고 성공적으로 넘어왔다면
     * onAuthenticationSuccess를 이용해 성공한 인증에 대한 처리 로직을 실행하고,
     * 실패한다면 authenticationFailureHandler를 이용해 실패 로직을 처리함
     * @return
     */
    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authenticationManager.setPasswordEncoder(passwordEncoder());
        return authenticationManager;
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
