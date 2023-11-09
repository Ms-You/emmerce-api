package commerce.emmerce.kakaopay.service;

import commerce.emmerce.config.SecurityUtil;
import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.domain.Member;
import commerce.emmerce.domain.OrderProduct;
import commerce.emmerce.kakaopay.dto.KakaoPayDTO;
import commerce.emmerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Transactional
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
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public Mono<KakaoPayDTO.ReadyResp> kakaoPayReady(KakaoPayDTO.PayReq payReq) {
        // 카카오페이 요청 양식
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("tax_free_amount", String.valueOf(0));    // 상품 비과세 금액
        params.add("approval_url", "http://localhost:8088/payment/success?orderId=" + payReq.getOrderId());   // 성공 시 redirect url
        params.add("cancel_url", "http://localhost:8088/payment/cancel"); // 취소 시 redirect url
        params.add("fail_url", "http://localhost:8088/payment/fail");   // 실패 시 redirect url

        return orderRepository.findById(payReq.getOrderId())
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

                    return productRepository.findById(orderProductList.get(0).getProductId())
                            .map(product -> {
                                String itemName = product.getName();

                                if (orderProductList.size() > 1) {
                                    itemName += " 외 " + (orderProductList.size() - 1) + "개";
                                }
                                params.add("item_name", itemName);

                                return orderProductList;
                            });
                })
                .flatMap(orderProductList -> findCurrentMember())
                .flatMap(member -> {
                    params.add("partner_user_id", String.valueOf(member.getMemberId()));    // 가맹점 회원 id

                    return webClient.post()
                            .uri(uriBuilder -> uriBuilder.path("/v1/payment/ready")
                                    .queryParams(params)
                                    .build())
                            .retrieve()
                            .bodyToMono(KakaoPayDTO.ReadyResp.class)
                            .flatMap(readyResp -> Mono.when(
                                    reactiveRedisTemplate.opsForValue().set("tid:" + member.getMemberId() + ":" + payReq.getOrderId(), readyResp.getTid()),
                                    reactiveRedisTemplate.opsForValue().set("partner_order_id:" + member.getMemberId(), params.getFirst("partner_order_id")),
                                    reactiveRedisTemplate.opsForValue().set("partner_user_id:" + member.getMemberId(), params.getFirst("partner_user_id"))
                            ).thenReturn(readyResp));
                });
    }


    public Mono<KakaoPayDTO.ApproveResp> kakaoPayApprove(String pgToken, Long orderId) {
        return findCurrentMember()
                .flatMap(member -> Mono.zip(
                                reactiveRedisTemplate.opsForValue().get("tid:" + member.getMemberId() + ":" + orderId),
                                reactiveRedisTemplate.opsForValue().get("partner_order_id:" + member.getMemberId()),
                                reactiveRedisTemplate.opsForValue().get("partner_user_id:" + member.getMemberId())
                        )
                        .flatMap(tuple -> {
                            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                            params.add("cid", cid);
                            params.add("tid", tuple.getT1());
                            params.add("partner_order_id", tuple.getT2());
                            params.add("partner_user_id", tuple.getT3());
                            params.add("pg_token", pgToken);

                            return webClient.post()
                                    .uri(uriBuilder -> uriBuilder.path("/v1/payment/approve")
                                            .queryParams(params)
                                            .build())
                                    .retrieve()
                                    .bodyToMono(KakaoPayDTO.ApproveResp.class)
                                    .flatMap(approveResp -> Mono.when(
                                                reactiveRedisTemplate.opsForValue().delete("partner_order_id:" + member.getMemberId()),
                                                reactiveRedisTemplate.opsForValue().delete("partner_user_id:" + member.getMemberId())
                                        ).thenReturn(approveResp)
                                    );
                        }));
    }


    public Mono<KakaoPayDTO.CancelResp> kakaoPayCancel(KakaoPayDTO.PayReq payReq) {
        return findCurrentMember()
                .flatMap(member -> orderRepository.findById(payReq.getOrderId())
                        .flatMap(order -> {
                            if(!order.getMemberId().equals(member.getMemberId())) {
                                return Mono.error(new GlobalException(ErrorCode.ORDER_MEMBER_NOT_MATCHED));
                            }

                            return reactiveRedisTemplate.opsForValue().get("tid:" + member.getMemberId() + ":" + order.getOrderId())
                                    .flatMap(tid -> {
                                        return orderProductRepository.findByOrderId(order.getOrderId()).collectList()
                                                .flatMap(orderProductList -> {
                                                    int cancelAmount = orderProductList.stream()
                                                            .mapToInt(OrderProduct::getTotalPrice)
                                                            .sum();

                                                    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                                                    params.add("cid", cid);
                                                    params.add("tid", tid);
                                                    params.add("cancel_amount", String.valueOf(cancelAmount));
                                                    params.add("cancel_tax_free_amount", String.valueOf(0));

                                                    return webClient.post()
                                                            .uri(uriBuilder -> uriBuilder.path("/v1/payment/cancel")
                                                                    .queryParams(params)
                                                                    .build())
                                                            .retrieve()
                                                            .bodyToMono(KakaoPayDTO.CancelResp.class)
                                                            .flatMap(cancelResp -> {
                                                                return reactiveRedisTemplate.opsForValue().delete("tid:" + member.getMemberId() + ":" + order.getOrderId())
                                                                        .thenReturn(cancelResp);
                                                            });
                                                });
                                    });

                        })
                );
    }


    private Mono<Member> findCurrentMember() {
        return SecurityUtil.getCurrentMemberName()
                .flatMap(name -> memberRepository.findByName(name));
    }

}
