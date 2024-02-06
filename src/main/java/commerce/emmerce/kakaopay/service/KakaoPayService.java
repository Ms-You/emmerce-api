package commerce.emmerce.kakaopay.service;

import commerce.emmerce.config.SecurityUtil;
import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.domain.*;
import commerce.emmerce.kakaopay.dto.CardInfo;
import commerce.emmerce.kakaopay.dto.KakaoPayDTO;
import commerce.emmerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Transactional
@RequiredArgsConstructor
@Service
public class KakaoPayService {

    private static final String cid = "TC0ONETIME"; // 테스트를 위한 가맹점 코드
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryRepository deliveryRepository;
    private final WebClient webClient;

    /**
     * 현재 로그인 한 사용자 정보 반환
     * @return
     */
    private Mono<Member> findCurrentMember() {
        return SecurityUtil.getCurrentMemberName()
                .flatMap(name -> memberRepository.findByName(name));
    }

    /**
     * 카카오페이 결제 준비 요청
     * @param payReq
     * @return
     */
    public Mono<KakaoPayDTO.ReadyResp> kakaoPayReady(KakaoPayDTO.PayReq payReq) {
        // 카카오페이 요청 양식
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("tax_free_amount", String.valueOf(0));    // 상품 비과세 금액
        params.add("approval_url", "https://emmerce1.duckdns.org/payment/success?orderId=" + payReq.getOrderId());   // 성공 시 redirect url
        params.add("cancel_url", "https://emmerce1.duckdns.org/payment/cancel"); // 취소 시 redirect url
        params.add("fail_url", "https://emmerce1.duckdns.org/payment/fail");   // 실패 시 redirect url

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
                            .flatMap(readyResp -> {
                                Payment payment = Payment.builder()
                                        .tid(readyResp.getTid())
                                        .cid(cid)
                                        .partner_order_id(String.valueOf(payReq.getOrderId()))
                                        .partner_user_id(String.valueOf(member.getMemberId()))
                                        .build();
                                return paymentRepository.saveTemporary(payment)
                                        .thenReturn(readyResp);
                            });
                });
    }

    /**
     * 카카오페이 결제 승인 요청
     * @param pgToken
     * @param orderId
     * @return
     */
    public Mono<KakaoPayDTO.ApproveResp> kakaoPayApprove(String pgToken, Long orderId) {
        return findCurrentMember()
                .flatMap(member -> paymentRepository.findByOrderId(orderId)
                        .flatMap(payment -> {
                            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                            params.add("cid", cid);
                            params.add("tid", payment.getTid());
                            params.add("partner_order_id", payment.getPartner_order_id());
                            params.add("partner_user_id", payment.getPartner_user_id());
                            params.add("pg_token", pgToken);

                            return webClient.post()
                                    .uri(uriBuilder -> uriBuilder.path("/v1/payment/approve")
                                            .queryParams(params)
                                            .build())
                                    .retrieve()
                                    .bodyToMono(KakaoPayDTO.ApproveResp.class)
                                    .flatMap(approveResp -> {
                                        Payment newPayment = convertToPayment(approveResp);
                                        return paymentRepository.save(newPayment)
                                                .then(updateOrderStatus(orderId, OrderStatus.COMPLETE))
                                                .then(updateProductStock(orderId, true))
                                                .thenReturn(approveResp);
                                    });
                        }));
    }

    /**
     * Payment 인스턴스 생성
     * @param approveResp
     * @return
     */
    private Payment convertToPayment(KakaoPayDTO.ApproveResp approveResp) {
        CardInfo cardInfo = approveResp.getCard_info();
        Optional<CardInfo> optionalCardInfo = Optional.ofNullable(cardInfo);
        System.out.println("cardInfo: " + cardInfo);

        return Payment.builder()
                .aid(approveResp.getAid())
                .tid(approveResp.getTid())
                .cid(approveResp.getCid())
                .partner_order_id(approveResp.getPartner_order_id())
                .partner_user_id(approveResp.getPartner_user_id())
                .payment_method_type(approveResp.getPayment_method_type())
                .total_amount(approveResp.getAmount().getTotal())
                .tax_free(approveResp.getAmount().getTax_free())
                .vat(approveResp.getAmount().getVat())
                .point(approveResp.getAmount().getPoint())
                .discount(approveResp.getAmount().getDiscount())
                .green_deposit(approveResp.getAmount().getGreen_deposit())
                .purchase_corp(optionalCardInfo.map(CardInfo::getPurchase_corp).orElse("-"))
                .purchase_corp_code(optionalCardInfo.map(CardInfo::getPurchase_corp_code).orElse("-"))
                .issuer_corp(optionalCardInfo.map(CardInfo::getIssuer_corp).orElse("-"))
                .issuer_corp_code(optionalCardInfo.map(CardInfo::getIssuer_corp_code).orElse("-"))
                .bin(optionalCardInfo.map(CardInfo::getBin).orElse("-"))
                .card_type(optionalCardInfo.map(CardInfo::getCard_type).orElse("-"))
                .install_month(optionalCardInfo.map(CardInfo::getInstall_month).orElse("-"))
                .approved_id(optionalCardInfo.map(CardInfo::getApproved_id).orElse("-"))
                .card_mid(optionalCardInfo.map(CardInfo::getCard_mid).orElse("-"))
                .interest_free_install(optionalCardInfo.map(CardInfo::getInterest_free_install).orElse("-"))
                .card_item_code(optionalCardInfo.map(CardInfo::getCard_item_code).orElse("-"))
                .item_name(approveResp.getItem_name())
                .quantity(approveResp.getQuantity())
                .created_at(LocalDateTime.parse(approveResp.getCreated_at()))
                .approved_at(LocalDateTime.parse(approveResp.getApproved_at()))
                .build();
    }

    /**
     * 주문 상태 정보 변경
     * @param orderId
     * @param orderStatus
     * @return
     */
    private Mono<Order> updateOrderStatus(Long orderId, OrderStatus orderStatus) {
        return orderRepository.findById(orderId)
                .flatMap(order -> {
                    order.updateStatus(orderStatus);
                    return orderRepository.save(order);
                });
    }

    /**
     * 상품 재고 수량 변경 (flag = true :결제 완료에 따른 재고 수량 변경)
     *                  (flag = false: 결제 취소에 따른 재고 수량 변경)
     * @param flag
     * @param orderId
     * @return
     */
    private Mono<Void> updateProductStock(Long orderId, boolean flag) {
        return orderProductRepository.findByOrderId(orderId)
                .flatMap(orderProduct -> productRepository.findById(orderProduct.getProductId())
                        .doOnSuccess(product -> {
                            int stockQuantity = flag ? product.getStockQuantity() - orderProduct.getTotalCount()
                                    : product.getStockQuantity() + orderProduct.getTotalCount();
                            product.updateStockQuantity(stockQuantity);
                        }).flatMap(productRepository::save)
                ).then();
    }

    /**
     * 카카오페이 결제 정보 조회
     * @param payReq
     * @return
     */
    public Mono<KakaoPayDTO.OrderResp> kakaoPayOrdered(KakaoPayDTO.PayReq payReq) {
        return findCurrentMember()
                .flatMap(member -> orderRepository.findById(payReq.getOrderId())
                        .flatMap(order -> {
                            if(!order.getMemberId().equals(member.getMemberId())) {
                                return Mono.error(new GlobalException(ErrorCode.ORDER_MEMBER_NOT_MATCHED));
                            }

                            return paymentRepository.findByOrderId(order.getOrderId())
                                    .flatMap(payment -> webClient.get()
                                            .uri(uriBuilder -> uriBuilder.path("/v1/payment/order")
                                                    .queryParam("cid", cid)
                                                    .queryParam("tid", payment.getTid())
                                                    .build())
                                            .retrieve()
                                            .bodyToMono(KakaoPayDTO.OrderResp.class));
                        })
                );
    }

    /**
     * 카카오페이 결제 취소
     * @param payReq
     * @return
     */
    public Mono<KakaoPayDTO.CancelResp> kakaoPayCancel(KakaoPayDTO.PayReq payReq) {
        return findCurrentMember()
                .flatMap(member -> orderRepository.findById(payReq.getOrderId())
                        .flatMap(order -> {
                            if (!order.getMemberId().equals(member.getMemberId())) {
                                return Mono.error(new GlobalException(ErrorCode.ORDER_MEMBER_NOT_MATCHED));
                            }

                            Mono<Order> updateOrder = Mono.just(order);
                            switch (order.getOrderStatus()) {
                                case CANCEL: return Mono.error(new GlobalException(ErrorCode.CANCELED_ORDER));
                                case ING: return Mono.error(new GlobalException(ErrorCode.ING_ORDER));
                                case COMPLETE: updateOrder = updateOrderStatus(order.getOrderId(), OrderStatus.CANCEL);
                            }

                            return updateOrder.flatMap(updated -> updateProductStock(order.getOrderId(), false))
                                    .then(updateDeliveryStatus(order.getOrderId()))
                                    .then(paymentRepository.findByOrderId(order.getOrderId()))
                                    .flatMap(payment -> orderProductRepository.findByOrderId(order.getOrderId()).collectList()
                                            .flatMap(orderProductList -> {
                                                int cancelAmount = orderProductList.stream()
                                                        .mapToInt(OrderProduct::getTotalPrice)
                                                        .sum();

                                                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                                                params.add("cid", cid);
                                                params.add("tid", payment.getTid());
                                                params.add("cancel_amount", String.valueOf(cancelAmount));
                                                params.add("cancel_tax_free_amount", String.valueOf(0));

                                                return webClient.post()
                                                        .uri(uriBuilder -> uriBuilder.path("/v1/payment/cancel")
                                                                .queryParams(params)
                                                                .build())
                                                        .retrieve()
                                                        .bodyToMono(KakaoPayDTO.CancelResp.class);
                                            }));
                        })
                );
    }

    /**
     * 주문 상태 변경
     * @param orderId
     * @return
     */
    private Mono<Void> updateDeliveryStatus(Long orderId) {
        return orderProductRepository.findByOrderId(orderId)
                .flatMap(orderProduct -> deliveryRepository.findByOrderProductId(orderProduct.getOrderProductId())
                        .flatMap(delivery -> deliveryRepository.updateStatus(delivery.getDeliveryId(), orderProduct.getOrderProductId(), DeliveryStatus.CANCEL))
                ).then();
    }


}
