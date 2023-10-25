package commerce.emmerce.config;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

public class SecurityUtil {

    private SecurityUtil(){

    }

    /**
     * 기본적으로 SecurityContextHolder는 ThreadLocal에 주체 정보를 저장해서
     * 같은 스레드의 실행 흐름 안에서는 항상 Security Context에 접근하는 것이 가능함
     * @return
     */
    public static Mono<String> getCurrentMemberName(){
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext -> SecurityContext.getAuthentication().getName())
                .switchIfEmpty(Mono.error(new RuntimeException("인증 정보가 없습니다.")));
    }

}
