package commerce.emmerce.service;

import commerce.emmerce.config.SecurityUtil;
import commerce.emmerce.domain.DeliveryStatus;
import commerce.emmerce.domain.Member;
import commerce.emmerce.domain.OrderProduct;
import commerce.emmerce.domain.Review;
import commerce.emmerce.dto.ReviewDTO;
import commerce.emmerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class ReviewService {

    private final MemberRepositoryImpl memberRepository;
    private final OrderProductRepositoryImpl orderProductRepository;
    private final ReviewRepositoryImpl reviewRepository;
    private final DeliveryRepositoryImpl deliveryRepository;

    @Transactional
    public Mono<Void> write(ReviewDTO.ReviewReq reviewReq) {
        return SecurityUtil.getCurrentMemberName()
                .flatMap(name -> memberRepository.findByName(name)
                        .flatMap(member -> getOrderProduct(member, reviewReq))
                );
    }

    public Mono<Void> getOrderProduct(Member member, ReviewDTO.ReviewReq reviewReq) {
        return orderProductRepository.findByOrderIdAndProductId(reviewReq.getOrderId(), reviewReq.getProductId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("잘못된 상품입니다.")))
                .flatMap(orderProduct -> checkDeliveryStatus(member, orderProduct, reviewReq));
    }

    public Mono<Void> checkDeliveryStatus(Member member, OrderProduct orderProduct, ReviewDTO.ReviewReq reviewReq) {
        return deliveryRepository.findByOrderId(orderProduct.getOrderId())
                .filter(delivery -> delivery.getDeliveryStatus().equals(DeliveryStatus.COMPLETE))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "배송이 완료된 다음에 작성해주세요.")))
                .flatMap(delivery -> writeReview(member, reviewReq));
    }

    public Mono<Void> writeReview(Member member, ReviewDTO.ReviewReq reviewReq) {
        return reviewRepository.save(Review.createReview()
                .title(reviewReq.getTitle())
                .description(reviewReq.getDescription())
                .startScore(reviewReq.getStarScore())
                .reviewImgList(reviewReq.getReviewImgList())
                .writeDate(LocalDate.now())
                .memberId(member.getMemberId())
                .productId(reviewReq.getProductId())
                .build());
    }



}
