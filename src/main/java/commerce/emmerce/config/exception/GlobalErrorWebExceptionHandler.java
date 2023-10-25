package commerce.emmerce.config.exception;

import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 *  전역 예외 처리를 커스터마이징
 *  DefaultErrorWebExceptionHandler 가 Order(-1)로 등록되어 있기 때문에
 *  이보다 앞서 동작하게 하기 위해서 Order(-2)로 정의
 */
@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalErrorWebExceptionHandler(GlobalErrorAttributes globalErrorAttributes,
                                          ApplicationContext applicationContext,
                                          ServerCodecConfigurer serverCodecConfigurer) {
        super(globalErrorAttributes, new Resources(), applicationContext);
        super.setMessageReaders(serverCodecConfigurer.getReaders());
        super.setMessageWriters(serverCodecConfigurer.getWriters());
    }

    /**
     * 라우터 별로 에러를 처리하는 라우팅 함수를 정의하는 메서드
     * @param errorAttributes the {@code ErrorAttributes} instance to use to extract error
     * information
     * @return
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * 라우터에서 에러가 발생하면 해당 메서드가 실행
     * @param request
     * @return
     */
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        // 생성자에서 GlobalErrorAttributes 를 받아 super 로 넘겨줘서
        // GlobalErrorAttributes 의 getErrorAttributes 메서드를 사용한 것
        final Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        return ServerResponse.status(Integer.parseInt(errorPropertiesMap.get("status").toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorPropertiesMap));
    }

}
