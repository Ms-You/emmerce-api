package commerce.emmerce.config.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

/**
 *  DefaultErrorAttributes: 예외에 대한 응답을 스프링이 자동으로 만들어줌
 *  스프링에서 예외가 발생하면 ErrorAttributes 객체로 등록된 빈을 통해 응답 값을 만드는데,
 *  기본 ErrorAttributes 가 autoConfiguration 으로 등록되어 제대로 된 응답 값을 주지 못함
 *
 *  GlobalErrorAttributes: 커스터마이징을 위해서 직접 ErrorAttributes 를 추가
 */
@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        /**
         * DefaultErrorAttributes.getErrorAttributes() 에서 기본적으로 보여주는 내용들
         * timestamp
         * path
         * status
         * error
         * message
         * requestId
         * exception
         * trace
         */
        Map<String, Object> map = super.getErrorAttributes(request, options);

        Throwable throwable = getError(request);
        if(throwable instanceof GlobalException) {
            GlobalException ex = (GlobalException) getError(request);
            map.put("exception", ex.getClass().getSimpleName());
            map.put("status", ex.getErrorCode().getStatus());
            map.put("message", ex.getErrorCode().getMessage());
        }

        return map;
    }

}
