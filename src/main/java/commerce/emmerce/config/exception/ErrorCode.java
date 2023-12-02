package commerce.emmerce.config.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 400
    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다"),
    // 405
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED.value(), "허용되지 않은 요청입니다."),
    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "내부 서버 오류입니다."),

    // Auth
    ACCESS_TOKEN_NOT_VALIDATE(HttpStatus.BAD_REQUEST.value(), "토큰이 유효하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST.value(), "만료된 토큰입니다."),
    ACCESS_TOKEN_UNSUPPORTED(HttpStatus.BAD_REQUEST.value(), "지원하지 않는 토큰입니다."),
    ACCESS_TOKEN_CLAIM_EMPTY(HttpStatus.BAD_REQUEST.value(), "JWT 클레임 문자열이 비었습니다."),
    REFRESH_TOKEN_NOT_VALIDATE(HttpStatus.BAD_REQUEST.value(), "리프레시 토큰이 유효하지 않습니다."),
    REFRESH_TOKEN_NOT_MATCHED(HttpStatus.BAD_REQUEST.value(), "리프레시 토큰이 일치하지 않습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 사용자입니다."),
    NAME_ALREADY_EXIST(HttpStatus.BAD_REQUEST.value(), "이미 존재하는 아이디입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST.value(), "비밀번호가 일치하지 않습니다."),

    // CartProduct
    CART_PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "장바구니에서 상품을 찾을 수 없습니다."),

    // Order
    CANCELED_ORDER(HttpStatus.BAD_REQUEST.value(), "이미 취소된 주문입니다."),
    ING_ORDER(HttpStatus.BAD_REQUEST.value(), "진행중인 주문입니다."),
    ORDER_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "해당 주문을 찾을 수 없습니다."),

    // Payment
    PAYMENT_CANCELED(HttpStatus.BAD_REQUEST.value(), "결제가 취소되었습니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST.value(), "결제에 실패하였습니다."),
    ORDER_MEMBER_NOT_MATCHED(HttpStatus.BAD_REQUEST.value(), "주문자가 일치하지 않습니다."),

    // Review
    AFTER_DELIVERY_COMPLETE(HttpStatus.BAD_REQUEST.value(), "배송이 완료된 다음에 작성해주세요."),
    DELIVERY_CANCELED(HttpStatus.BAD_REQUEST.value(), "배송이 취소되어 리뷰를 작성할 수 없습니다."),
    AFTER_ORDER_COMPLETE(HttpStatus.BAD_REQUEST.value(), "주문이 완료된 다음에 작성해주세요."),
    ORDER_CANCELED(HttpStatus.BAD_REQUEST.value(), "주문이 취소되어 리뷰를 작성할 수 없습니다."),
    ALREADY_WROTE(HttpStatus.BAD_REQUEST.value(), "작성한 리뷰가 존재합니다."),
    ;

    private final int status;
    private final String message;

    ErrorCode(final int status, final String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

}
