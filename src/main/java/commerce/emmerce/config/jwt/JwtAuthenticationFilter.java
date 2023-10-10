package commerce.emmerce.config.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveToken(exchange.getRequest());
        if (StringUtils.hasText(token)) {
            return tokenProvider.validateToken(token)
                    .filter(Boolean::booleanValue)  // true 값을 가진 객체만 필터링
                    .flatMap(valid -> {
                        if (valid) {
                            return tokenProvider.getAuthentication(token);
                        } else {
                            // 유효하지 않은 토큰인 경우 에러 반환
                            return Mono.error(new IllegalArgumentException("유효하지 않은 토큰입니다."));
                        }
                    })
                    .flatMap(authentication -> chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
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
