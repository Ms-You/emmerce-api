package commerce.emmerce.service;

import commerce.emmerce.config.FileHandler;
import commerce.emmerce.config.SecurityUtil;
import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.domain.DeliveryStatus;
import commerce.emmerce.domain.Member;
import commerce.emmerce.domain.OrderProduct;
import commerce.emmerce.domain.Review;
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

    private final FileHandler fileHandler;
    private final MemberRepository memberRepository;
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
     * @return
     */
    @Transactional
    public Mono<Void> write(Mono<ReviewDTO.ReviewReq> reviewReqMono,
                            Flux<FilePart> reviewImgs) {
        return findCurrentMember()
                .flatMap(member -> getOrderProduct(member, reviewReqMono))
                .flatMap(member -> writeReview(member, reviewReqMono, reviewImgs));
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
        return deliveryRepository.findByOrderId(orderProduct.getOrderId())
                .filter(delivery -> delivery.getDeliveryStatus().equals(DeliveryStatus.COMPLETE))
                .switchIfEmpty(Mono.error(new GlobalException(ErrorCode.AFTER_DELIVERY)))
                .thenReturn(member);
    }

    /**
     * 리뷰 작성
     * @param member
     * @param reviewReqMono
     * @param reviewImgs
     * @return
     */
    public Mono<Void> writeReview(Member member, Mono<ReviewDTO.ReviewReq> reviewReqMono, Flux<FilePart> reviewImgs) {
        return reviewReqMono.flatMap(reviewReq -> {
            // 임시 저장 경로
            String imagePath = "C:\\emmerce\\images\\";

            return fileHandler.savedImagesAndGetPaths(reviewImgs, imagePath)
                    .flatMap(savedReviewImgs -> reviewRepository.save(Review.createReview()
                            .title(reviewReq.getTitle())
                            .description(reviewReq.getDescription())
                            .startScore(reviewReq.getStarScore())
                            .reviewImgList(savedReviewImgs)
                            .writeDate(LocalDate.now())
                            .memberId(member.getMemberId())
                            .productId(reviewReq.getProductId())
                            .build()));
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
                .flatMap(review -> fileHandler.deleteImages(review.getReviewImgList())
                        .then(Mono.just(review)))
                .flatMap(review -> reviewRepository.deleteById(reviewId))
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
