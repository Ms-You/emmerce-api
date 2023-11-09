package commerce.emmerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class KakaoPayDTO {

    @Getter
    @NoArgsConstructor
    public static class ReadyReq {
        private Long orderId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReadyResp {
        private String tid; // 결제 고유 번호
        private String next_redirect_pc_url;    // pc 웹일 경우 사용자 정보 입력 화면
        private String created_at;  // 결제 준비 요청 시간
    }


}
