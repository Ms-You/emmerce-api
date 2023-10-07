package commerce.emmerce.config;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

public class SecurityUtil {

    private SecurityUtil(){

    }

    public static Mono<String> getCurrentMemberName(){
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext -> SecurityContext.getAuthentication().getName())
                .switchIfEmpty(Mono.error(new RuntimeException("인증 정보가 없습니다.")));
    }

}
