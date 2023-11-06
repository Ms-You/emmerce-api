package commerce.emmerce.config.jwt;

import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final TokenProvider tokenProvider;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveToken(exchange.getRequest());
        if (StringUtils.hasText(token)) {
            return reactiveRedisTemplate.opsForValue().get(token)
                    .defaultIfEmpty("active")
                    .flatMap(logoutStatus -> {
                        if ("logout".equals(logoutStatus)) {
                            return Mono.error(new GlobalException(ErrorCode.ACCESS_TOKEN_NOT_VALIDATE));
                        }
                        return tokenProvider.validateToken(token)
                                .filter(Boolean::booleanValue)  // true 값을 가진 객체만 필터링
                                .flatMap(valid -> {
                                    if (valid) {
                                        return tokenProvider.getAuthentication(token);
                                    } else {
                                        return Mono.error(new GlobalException(ErrorCode.ACCESS_TOKEN_NOT_VALIDATE));
                                    }
                                })
                                .flatMap(authentication -> chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                                );
                    });
        }

        return chain.filter(exchange);
    }



    /**
     * 토큰 추출
     * @param request
     * @return
     */
    public String resolveToken(ServerHttpRequest request){
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }

        return null;
    }

}
