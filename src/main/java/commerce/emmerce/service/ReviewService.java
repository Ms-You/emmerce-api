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

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {

    private final S3FileUploader s3FileUploader;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
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
        return reviewReqMono.flatMap(reviewReq -> orderProductRepository.findById(reviewReq.getOrderProductId())
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
        return deliveryRepository.findByOrderProductId(orderProduct.getOrderProductId())
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
        return reviewReqMono.flatMap(reviewReq -> orderProductRepository.findById(reviewReq.getOrderProductId())
                .flatMap(orderProduct -> orderRepository.findById(orderProduct.getOrderId())
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
                )
        );
    }

    /**
     * 리뷰 작성 여부 조회
     * @param member
     * @param reviewReqMono
     * @return
     */
    public Mono<Member> checkAlreadyWrote(Member member, Mono<ReviewDTO.ReviewReq> reviewReqMono) {
        return reviewReqMono.flatMap(reviewReq -> reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), reviewReq.getOrderProductId())
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
                .flatMap(tuple -> orderProductRepository.findById(tuple.getT1().getOrderProductId())
                        .flatMap(orderProduct -> reviewRepository.save(Review.createReview()
                                        .title(tuple.getT1().getTitle())
                                        .description(tuple.getT1().getDescription())
                                        .ratings(tuple.getT1().getRatings())
                                        .reviewImgList(tuple.getT2())
                                        .writeDate(LocalDateTime.now())
                                        .memberId(member.getMemberId())
                                        .productId(orderProduct.getProductId())
                                        .orderProductId(tuple.getT1().getOrderProductId())
                                        .build())
                                .then(updateStarScore(orderProduct.getProductId(), tuple.getT1().getRatings(), true)))
                );
    }


    /**
     * 상품 별점 및 리뷰 수 업데이트
     * @param productId
     * @param ratings
     * @param addStatus (true: 리뷰 작성, false: 리뷰 삭제)
     * @return
     */
    public Mono<Void> updateStarScore(Long productId, Ratings ratings, boolean addStatus) {
        return productRepository.findById(productId)
                .flatMap(product -> {
                    double totalRatings = product.getStarScore() * product.getTotalReviews();
                    int totalReviews = product.getTotalReviews();

                    if(addStatus) {
                        totalRatings += ratings.getValue();
                        totalReviews++;
                    } else {
                        totalRatings -= ratings.getValue();
                        totalReviews--;

                        if(totalReviews < 0 || totalRatings < 0) {
                            totalRatings = 0.0;
                            totalReviews = 0;
                        }
                    }

                    double newStarScore = totalReviews > 0 ? totalRatings / totalReviews : 0;
                    newStarScore = Math.round(newStarScore * 10) / 10.0;    // 소수점 둘 째 자리에서 반올림

                    product.updateStarScore(newStarScore);
                    product.updateTotalReviews(totalReviews);

                    return productRepository.save(product);
                }).then();
    }


    /**
     * 리뷰 삭제
     * @param reviewId
     * @return
     */
    @Transactional
    public Mono<Void> remove(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new GlobalException(ErrorCode.REVIEW_NOT_FOUND)))
                .flatMap(review -> s3FileUploader.deleteS3ImageList(review.getReviewImgList(), "review")
                        .then(reviewRepository.deleteById(reviewId))
                        .then(updateStarScore(review.getProductId(), review.getRatings(), false))
                ).then();
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
                .map(reviewResp -> reviewResp.toBuilder().writer(maskingMemberName(reviewResp.getWriter())).build());
        /*
            reviewRepository.findAllByProductId() 메서드가 Flux<ReviewResp> 객체를 반환함
            그러다보니 리뷰 작성자 명을 마스킹하지 못하는 문제 발생
            repository 에서 masking 을 해주자니 각 계층 별 책임을 명확히 하지 못함
            그렇다고 findAllByProductId() 메서드가 Flux<Review> 를 반환하도록 하면
            결국 review.getMemberId() 를 통해 member 객체를 찾아 마스킹 해줘야하므로 DB 접근 횟수 증가
            결과적으로 DB 접근을 최소화하며 이를 해결하기 위해 toBuilder 사용
            - ReviewResp DTO @Builder(toBuilder = true) 옵션 추가
        */

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
