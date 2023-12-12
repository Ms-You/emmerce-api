package commerce.emmerce.service;

import commerce.emmerce.config.s3.S3FileUploader;
import commerce.emmerce.config.SecurityUtil;
import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.domain.*;
import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.ReviewDTO;
import commerce.emmerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {

    private final S3FileUploader s3FileUploader;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ReviewRepository reviewRepository;
    private final DeliveryRepository deliveryRepository;

    /**
     * 현재 로그인 한 사용자 정보 반환
     * @return
     */
    private Mono<Member> findCurrentMember() {
        return SecurityUtil.getCurrentMemberName()
                .flatMap(name -> memberRepository.findByName(name));
    }

    /**
     * 리뷰 작성
     * @param reviewReqMono
     * @param reviewImages
     * @return
     */
    @Transactional
    public Mono<Void> write(Mono<ReviewDTO.ReviewReq> reviewReqMono,
                            Flux<FilePart> reviewImages) {
        return findCurrentMember()
                .flatMap(member -> getOrderProduct(member, reviewReqMono))
                .flatMap(member -> checkOrderStatus(member, reviewReqMono))
                .flatMap(member -> checkAlreadyWrote(member, reviewReqMono))
                .flatMap(member -> writeReview(member, reviewReqMono, reviewImages));
    }

    /**
     * 주문 상품 조회
     * @param member
     * @param reviewReqMono
     * @return
     */
    public Mono<Member> getOrderProduct(Member member, Mono<ReviewDTO.ReviewReq> reviewReqMono) {
        return reviewReqMono.flatMap(reviewReq -> orderProductRepository.findByOrderIdAndProductId(reviewReq.getOrderId(), reviewReq.getProductId())
                .switchIfEmpty(Mono.error(new GlobalException(ErrorCode.ORDER_NOT_FOUND)))
                .flatMap(orderProduct -> checkDeliveryStatus(member, orderProduct))
        );
    }

    /**
     * 배송 상태 조회
     * @param member
     * @param orderProduct
     * @return
     */
    public Mono<Member> checkDeliveryStatus(Member member, OrderProduct orderProduct) {
        return deliveryRepository.findByOrderIdAndProductId(orderProduct.getOrderId(), orderProduct.getProductId())
                .flatMap(delivery -> {
                    DeliveryStatus status = delivery.getDeliveryStatus();
                    if(status.equals(DeliveryStatus.COMPLETE)) {
                        return Mono.just(member);
                    } else if(status.equals(DeliveryStatus.CANCEL)) {
                        return Mono.error(new GlobalException(ErrorCode.DELIVERY_CANCELED));
                    } else {
                        return Mono.error(new GlobalException(ErrorCode.AFTER_DELIVERY_COMPLETE));
                    }
                });
    }

    /**
     * 주문 상태 조회
     * @param member
     * @param reviewReqMono
     * @return
     */
    public Mono<Member> checkOrderStatus(Member member, Mono<ReviewDTO.ReviewReq> reviewReqMono) {
        return reviewReqMono.flatMap(reviewReq -> orderRepository.findById(reviewReq.getOrderId())
                .flatMap(order -> {
                    OrderStatus status = order.getOrderStatus();
                    if(status.equals(OrderStatus.COMPLETE)) {
                        return Mono.just(member);
                    } else if(status.equals(OrderStatus.CANCEL)) {
                        return Mono.error(new GlobalException(ErrorCode.ORDER_CANCELED));
                    } else {
                        return Mono.error(new GlobalException(ErrorCode.AFTER_ORDER_COMPLETE));
                    }
                })
        );
    }

    /**
     * 리뷰 작성 여부 조회
     * @param member
     * @param reviewReqMono
     * @return
     */
    public Mono<Member> checkAlreadyWrote(Member member, Mono<ReviewDTO.ReviewReq> reviewReqMono) {
        return reviewReqMono.flatMap(reviewReq -> reviewRepository.findByMemberAndProduct(member.getMemberId(), reviewReq.getProductId())
                .flatMap(count -> {
                    if (count == 0) {
                        return Mono.just(member);
                    } else {
                        return Mono.error(new GlobalException(ErrorCode.ALREADY_WROTE));
                    }
                })
        );
    }

    /**
     * 리뷰 작성
     * @param member
     * @param reviewReqMono
     * @param reviewImages
     * @return
     */
    public Mono<Void> writeReview(Member member, Mono<ReviewDTO.ReviewReq> reviewReqMono, Flux<FilePart> reviewImages) {
        return reviewReqMono.zipWith(s3FileUploader.uploadS3ImageList(reviewImages, "review"))
                .flatMap(tuple -> reviewRepository.save(Review.createReview()
                        .title(tuple.getT1().getTitle())
                        .description(tuple.getT1().getDescription())
                        .startScore(tuple.getT1().getStarScore())
                        .reviewImgList(tuple.getT2())
                        .writeDate(LocalDate.now())
                        .memberId(member.getMemberId())
                        .productId(tuple.getT1().getProductId())
                        .build()));
    }

    /**
     * 리뷰 삭제
     * @param reviewId
     * @return
     */
    @Transactional
    public Mono<Void> remove(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .flatMap(review -> s3FileUploader.deleteS3ImageList(review.getReviewImgList(), "review")
                        .then(reviewRepository.deleteById(reviewId)))
                .doOnNext(rowsUpdated -> log.info("삭제된 리뷰 수: {}", rowsUpdated))
                .then();
    }

    /**
     * 상품에 속한 리뷰 목록 조회 (페이징)
     * @param productId
     * @param page
     * @param size
     * @return
     */
    public Mono<PageResponseDTO<ReviewDTO.ReviewResp>> reviewsByProduct(Long productId, Integer page, Integer size) {
        Mono<Long> totalReviews = reviewRepository.reviewCountByProduct(productId);
        Flux<ReviewDTO.ReviewResp> reviewRespFlux = reviewRepository.findAllByProductId(productId)
                .skip((page-1) * size)
                .take(size)
                .flatMap(review -> memberRepository.findById(review.getMemberId())
                        .map(member -> ReviewDTO.ReviewResp.builder()
                                .reviewId(review.getReviewId())
                                .title(review.getTitle())
                                .description(review.getDescription())
                                .starScore(review.getStarScore())
                                .reviewImgList(review.getReviewImgList())
                                .writeDate(review.getWriteDate())
                                .memberId(review.getMemberId())
                                .writer(maskingMemberName(member.getName()))
                                .build())
                );

        return Mono.zip(reviewRespFlux.collectList(), totalReviews)
                .map(t -> new PageResponseDTO<>(t.getT1(), page, size, t.getT2().intValue()));
    }

    /**
     * 사용자 이름 마스킹 처리
     * @param existsName
     * @return
     */
    private String maskingMemberName(String existsName) {
        StringBuilder sb = new StringBuilder();
        sb.append(existsName.substring(0,1));
        sb.append("********");
        sb.append(existsName.substring(existsName.length() - 1));

        return sb.toString();
    }

}
