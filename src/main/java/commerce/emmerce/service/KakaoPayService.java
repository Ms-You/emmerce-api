package commerce.emmerce.service;

import commerce.emmerce.config.SecurityUtil;
import commerce.emmerce.domain.OrderProduct;
import commerce.emmerce.dto.KakaoPayDTO;
import commerce.emmerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class KakaoPayService {

    private static final String cid = "TC0ONETIME"; // 테스트를 위한 가맹점 코드
    private static final String kakaoPayUrl = "https://kapi.kakao.com";
    @Value("${kakao.admin-key}")
    private String adminKey;

    private final MemberRepositoryImpl memberRepository;
    private final OrderRepositoryImpl orderRepository;
    private final OrderProductRepositoryImpl orderProductRepository;
    private final ProductRepositoryImpl productRepository;
    private final WebClient webClient;

    public Mono<KakaoPayDTO.ReadyResp> kakaoPayReady(KakaoPayDTO.ReadyReq readyReq) {
        // 카카오페이 요청 양식
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("tax_free_amount", String.valueOf(0));    // 상품 비과세 금액
        params.add("approval_url", "http://localhost:8088/payment/success");   // 성공 시 redirect url
        params.add("cancel_url", "http://localhost:8088/payment/cancel"); // 취소 시 redirect url
        params.add("fail_url", "http://localhost:8088/payment/fail");   // 실패 시 redirect url

        return orderRepository.findById(readyReq.getOrderId())
                .flatMap(order -> {
                    params.add("partner_order_id", String.valueOf(order.getOrderId()));   // 가맹점 주문 번호
                    return orderProductRepository.findByOrderId(order.getOrderId()).collectList();
                })
                .flatMap(orderProductList -> {
                    int totalQuantity = orderProductList.stream()
                            .mapToInt(OrderProduct::getTotalCount)
                            .sum();

                    int totalAmount = orderProductList.stream()
                            .mapToInt(OrderProduct::getTotalPrice)
                            .sum();

                    params.add("quantity", String.valueOf(totalQuantity));   // 주문 수량
                    params.add("total_amount", String.valueOf(totalAmount));   // 총 금액

                    return productRepository.findById(orderProductList.get(0).getProductId());
                })
                .flatMap(product -> {
                    params.add("item_name", product.getName());  // 상품 명
                    return SecurityUtil.getCurrentMemberName()
                            .flatMap(name -> memberRepository.findByName(name));
                })
                .map(member -> {
                    params.add("partner_user_id", String.valueOf(member.getMemberId()));    // 가맹점 회원 id
                    return params;
                })
                .flatMap(p -> webClient.post()
                        .uri(uriBuilder -> uriBuilder.path("/v1/payment/ready")
                                .queryParams(params)
                                .build())
                        .retrieve()
                        .bodyToMono(KakaoPayDTO.ReadyResp.class));
    }

}
