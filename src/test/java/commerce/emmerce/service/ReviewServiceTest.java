package commerce.emmerce.service;

import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.config.s3.S3FileUploader;
import commerce.emmerce.domain.*;
import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.ReviewDTO;
import commerce.emmerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static commerce.emmerce.common.MemberUtil.maskingMemberName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(ReviewService.class)
class ReviewServiceTest {
    @Autowired
    private ReviewService reviewService;

    @MockBean
    private S3FileUploader s3FileUploader;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderProductRepository orderProductRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private ReviewRepository reviewRepository;

    @MockBean
    private DeliveryRepository deliveryRepository;


    private Member member;
    private Order order;
    private Product product;
    private OrderProduct orderProduct;
    private Delivery delivery;
    private Review review;

    private SecurityContext securityContext;

    @BeforeEach
    void setup() {
        member = Member.createMember()
                .id(1L)
                .name("testId001")
                .email("test@test.com")
                .password("password")
                .tel("01012345678")
                .birth("240422")
                .point(0)
                .role(RoleType.ROLE_USER)
                .city("서울특별시")
                .street("공룡로 50")
                .zipcode("18888")
                .build();

        order = Order.createOrder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETE)
                .memberId(member.getMemberId())
                .build();

        product = Product.createProduct()
                .productId(1L)
                .name("바지")
                .detail("찢어진 바지")
                .originalPrice(10000)
                .discountPrice(8000)
                .discountRate(20)
                .stockQuantity(100)
                .starScore(4.5)
                .totalReviews(20)
                .titleImg("바지 이미지")
                .detailImgList(List.of("바지 상세 이미지1", "바지 상세 이미지2"))
                .brand("이머스몰")
                .enrollTime(LocalDateTime.now())
                .build();

        orderProduct = OrderProduct.builder()
                .orderProductId(1L)
                .totalPrice(40000)
                .totalCount(5)
                .orderId(order.getOrderId())
                .productId(product.getProductId())
                .build();

        delivery = Delivery.createDelivery()
                .deliveryId(1L)
                .name("tester001")
                .tel("01012345678")
                .email("test@test.com")
                .city("서울특별시")
                .street("공룡로 50")
                .zipcode("18888")
                .deliveryStatus(DeliveryStatus.COMPLETE)
                .orderProductId(orderProduct.getOrderProductId())
                .build();

        review = Review.createReview()
                .reviewId(1L)
                .title("바지 리뷰")
                .description("찢어진 바지가 왔네요. 제가 스폰지밥인가요?")
                .ratings(Ratings.ONE)
                .reviewImgList(List.of("바지 리뷰 이미지1", "바지 리뷰 이미지2"))
                .writeDate(LocalDateTime.now())
                .memberId(member.getMemberId())
                .productId(product.getProductId())
                .orderProductId(orderProduct.getOrderProductId())
                .build();


        when(memberRepository.findByName(member.getName())).thenReturn(Mono.just(member));

        Authentication authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(member.getName());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(securityContext.getAuthentication().getName()).thenReturn(member.getName());
    }


    @Test
    @DisplayName("리뷰 작성 총괄 테스트")
    void write() {
        // given
        ReviewDTO.ReviewReq reviewReq = new ReviewDTO.ReviewReq(review.getTitle(), review.getDescription(),
                review.getRatings(), review.getOrderProductId());

        // when
        when(orderProductRepository.findById(reviewReq.getOrderProductId())).thenReturn(Mono.just(orderProduct));
        when(deliveryRepository.findByOrderProductId(orderProduct.getOrderProductId())).thenReturn(Mono.just(delivery));
        when(orderRepository.findById(orderProduct.getOrderId())).thenReturn(Mono.just(order));
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), reviewReq.getOrderProductId())).thenReturn(Mono.just(0L));
        when(s3FileUploader.uploadS3ImageList(any(), anyString())).thenReturn(Mono.just(List.of("reviewImageUrl1", "reviewImageUrl2")));
        when(reviewRepository.save(any(Review.class))).thenReturn(Mono.empty());
        when(productRepository.findById(product.getProductId())).thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.empty());

        StepVerifier.create(reviewService.write(Mono.just(reviewReq), null)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        ).verifyComplete();

        // then
        verify(orderProductRepository, times(3)).findById(anyLong());
        verify(deliveryRepository, times(1)).findByOrderProductId(anyLong());
        verify(orderRepository, times(1)).findById(anyLong());
        verify(reviewRepository, times(1)).findByMemberAndOrderProduct(anyLong(), anyLong());
        verify(s3FileUploader, times(1)).uploadS3ImageList(any(), anyString());
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("주문 상태 조회 테스트")
    void getOrderProduct() {
        // given
        ReviewDTO.ReviewReq reviewReq = new ReviewDTO.ReviewReq(review.getTitle(), review.getDescription(),
                review.getRatings(), review.getOrderProductId());

        // when
        when(orderProductRepository.findById(reviewReq.getOrderProductId())).thenReturn(Mono.just(orderProduct));
        when(deliveryRepository.findByOrderProductId(orderProduct.getOrderProductId())).thenReturn(Mono.just(delivery));

        StepVerifier.create(reviewService.getOrderProduct(member, Mono.just(reviewReq)))
                .expectNext(member)
                .verifyComplete();

        // then
        verify(orderProductRepository, times(1)).findById(anyLong());
        verify(deliveryRepository, times(1)).findByOrderProductId(anyLong());
    }

    @Test
    @DisplayName("주문 상태 조회 테스트 - 실패 (주문을 찾지 못함)")
    void getOrderProduct_failure() {
        // given
        ReviewDTO.ReviewReq reviewReq = new ReviewDTO.ReviewReq(review.getTitle(), review.getDescription(),
                review.getRatings(), review.getOrderProductId());

        // when
        when(orderProductRepository.findById(reviewReq.getOrderProductId())).thenReturn(Mono.empty());

        StepVerifier.create(reviewService.getOrderProduct(member, Mono.just(reviewReq)))
                .expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.ORDER_NOT_FOUND)
                .verify();

        // then
        verify(orderProductRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("배송 상품 조회 테스트 - 성공")
    void checkDeliveryStatus_success() {
        // given
        // when
        when(deliveryRepository.findByOrderProductId(orderProduct.getOrderProductId())).thenReturn(Mono.just(delivery));

        StepVerifier.create(reviewService.checkDeliveryStatus(member, orderProduct))
                .expectNext(member)
                .verifyComplete();

        // then
        verify(deliveryRepository, times(1)).findByOrderProductId(anyLong());
    }

    @Test
    @DisplayName("배송 상품 조회 테스트 - 실패 (취소된 배송)")
    void checkDeliveryStatus_failure_delivery_canceled() {
        // given
        delivery.updateStatus(DeliveryStatus.CANCEL);

        // when
        when(deliveryRepository.findByOrderProductId(orderProduct.getOrderProductId())).thenReturn(Mono.just(delivery));

        StepVerifier.create(reviewService.checkDeliveryStatus(member, orderProduct))
                .expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.DELIVERY_CANCELED)
                .verify();

        // then
        verify(deliveryRepository, times(1)).findByOrderProductId(anyLong());
    }

    @Test
    @DisplayName("배송 상품 조회 테스트 - 실패 (완료되지 않은 배송)")
    void checkDeliveryStatus_failure_delivery_not_complete() {
        // given
        delivery.updateStatus(DeliveryStatus.ING);

        // when
        when(deliveryRepository.findByOrderProductId(orderProduct.getOrderProductId())).thenReturn(Mono.just(delivery));

        StepVerifier.create(reviewService.checkDeliveryStatus(member, orderProduct))
                .expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.AFTER_DELIVERY_COMPLETE)
                .verify();

        // then
        verify(deliveryRepository, times(1)).findByOrderProductId(anyLong());
    }

    @Test
    @DisplayName("주문 상태 조회 테스트 - 성공")
    void checkOrderStatus_success() {
        // given
        ReviewDTO.ReviewReq reviewReq = new ReviewDTO.ReviewReq(review.getTitle(), review.getDescription(),
                review.getRatings(), review.getOrderProductId());

        // when
        when(orderProductRepository.findById(reviewReq.getOrderProductId())).thenReturn(Mono.just(orderProduct));
        when(orderRepository.findById(orderProduct.getOrderId())).thenReturn(Mono.just(order));

        StepVerifier.create(reviewService.checkOrderStatus(member, Mono.just(reviewReq)))
                .expectNext(member)
                .verifyComplete();


        // then
        verify(orderProductRepository, times(1)).findById(anyLong());
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("주문 상태 조회 테스트 - 실패 (사용자가 일치하지 않음)")
    void checkOrderStatus_failure_member_not_matched() {
        // given
        order = Order.createOrder()
                .orderId(1L)
                .orderDate(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETE)
                .memberId(2L)
                .build();

        ReviewDTO.ReviewReq reviewReq = new ReviewDTO.ReviewReq(review.getTitle(), review.getDescription(),
                review.getRatings(), review.getOrderProductId());

        // when
        when(orderProductRepository.findById(reviewReq.getOrderProductId())).thenReturn(Mono.just(orderProduct));
        when(orderRepository.findById(orderProduct.getOrderId())).thenReturn(Mono.just(order));

        StepVerifier.create(reviewService.checkOrderStatus(member, Mono.just(reviewReq)))
                .expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.ORDER_MEMBER_NOT_MATCHED)
                .verify();

        // then
        verify(orderProductRepository, times(1)).findById(anyLong());
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("주문 상태 조회 테스트 - 실패 (취소된 주문)")
    void checkOrderStatus_failure_order_canceled() {
        // given
        order.updateStatus(OrderStatus.CANCEL);

        ReviewDTO.ReviewReq reviewReq = new ReviewDTO.ReviewReq(review.getTitle(), review.getDescription(),
                review.getRatings(), review.getOrderProductId());

        // when
        when(orderProductRepository.findById(reviewReq.getOrderProductId())).thenReturn(Mono.just(orderProduct));
        when(orderRepository.findById(orderProduct.getOrderId())).thenReturn(Mono.just(order));

        StepVerifier.create(reviewService.checkOrderStatus(member, Mono.just(reviewReq)))
                .expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.ORDER_CANCELED)
                .verify();

        // then
        verify(orderProductRepository, times(1)).findById(anyLong());
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("주문 상태 조회 테스트 - 실패 (완료되지 않은 주문)")
    void checkOrderStatus_failure_order_not_complete() {
        // given
        order.updateStatus(OrderStatus.ING);

        ReviewDTO.ReviewReq reviewReq = new ReviewDTO.ReviewReq(review.getTitle(), review.getDescription(),
                review.getRatings(), review.getOrderProductId());

        // when
        when(orderProductRepository.findById(reviewReq.getOrderProductId())).thenReturn(Mono.just(orderProduct));
        when(orderRepository.findById(orderProduct.getOrderId())).thenReturn(Mono.just(order));

        StepVerifier.create(reviewService.checkOrderStatus(member, Mono.just(reviewReq)))
                .expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.AFTER_ORDER_COMPLETE)
                .verify();

        // then
        verify(orderProductRepository, times(1)).findById(anyLong());
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("리뷰 작성 여부 조회 테스트 - 아직 작성 안함")
    void checkAlreadyWrote_not_yet() {
        // given
        ReviewDTO.ReviewReq reviewReq = new ReviewDTO.ReviewReq(review.getTitle(), review.getDescription(),
                review.getRatings(), review.getOrderProductId());

        // when
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), reviewReq.getOrderProductId())).thenReturn(Mono.just(0L));

        StepVerifier.create(reviewService.checkAlreadyWrote(member, Mono.just(reviewReq)))
                .expectNext(member)
                .verifyComplete();

        // then
        verify(reviewRepository, times(1)).findByMemberAndOrderProduct(anyLong(), anyLong());
    }

    @Test
    @DisplayName("리뷰 작성 여부 조회 테스트 - 이미 작성함")
    void checkAlreadyWrote_already() {
        // given
        ReviewDTO.ReviewReq reviewReq = new ReviewDTO.ReviewReq(review.getTitle(), review.getDescription(),
                review.getRatings(), review.getOrderProductId());

        // when
        when(reviewRepository.findByMemberAndOrderProduct(member.getMemberId(), reviewReq.getOrderProductId())).thenReturn(Mono.just(1L));

        StepVerifier.create(reviewService.checkAlreadyWrote(member, Mono.just(reviewReq)))
                .expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.ALREADY_WROTE)
                .verify();

        // then
        verify(reviewRepository, times(1)).findByMemberAndOrderProduct(anyLong(), anyLong());
    }

    @Test
    @DisplayName("리뷰 작성 테스트")
    void writeReview() {
        // given
        ReviewDTO.ReviewReq reviewReq = new ReviewDTO.ReviewReq(review.getTitle(), review.getDescription(),
                review.getRatings(), review.getOrderProductId());

        // when
        when(s3FileUploader.uploadS3ImageList(any(), anyString())).thenReturn(Mono.just(List.of("reviewImageUrl1", "reviewImageUrl2")));
        when(orderProductRepository.findById(review.getOrderProductId())).thenReturn(Mono.just(orderProduct));
        when(reviewRepository.save(any(Review.class))).thenReturn(Mono.empty());
        when(productRepository.findById(product.getProductId())).thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.empty());

        StepVerifier.create(reviewService.writeReview(member, Mono.just(reviewReq), null))
                .verifyComplete();

        // then
        verify(s3FileUploader, times(1)).uploadS3ImageList(any(), anyString());
        verify(orderProductRepository, times(1)).findById(anyLong());
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 별점 및 리뷰 개수 수정 테스트")
    void updateStarScore() {
        // given
        Ratings ratings = Ratings.FIVE;
        boolean addStatus = false;

        // when
        when(productRepository.findById(product.getProductId())).thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.empty());

        StepVerifier.create(reviewService.updateStarScore(product.getProductId(), ratings, addStatus))
                .verifyComplete();

        // then
        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("리뷰 삭제 테스트 - 성공")
    void remove_success() {
        // given
        // when
        when(reviewRepository.findById(review.getReviewId())).thenReturn(Mono.just(review));
        when(s3FileUploader.deleteS3ImageList(eq(review.getReviewImgList()), anyString())).thenReturn(Flux.empty());
        when(reviewRepository.deleteById(review.getReviewId())).thenReturn(Mono.just(1L));
        when(productRepository.findById(product.getProductId())).thenReturn(Mono.just(product));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.empty());

        StepVerifier.create(reviewService.remove(review.getReviewId()))
                .verifyComplete();

        // then
        verify(reviewRepository, times(1)).findById(anyLong());
        verify(s3FileUploader, times(1)).deleteS3ImageList(anyList(), anyString());
        verify(reviewRepository, times(1)).deleteById(anyLong());
        verify(productRepository, times(1)).findById(anyLong());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("리뷰 삭제 테스트 - 실패 (리뷰를 찾을 수 없음)")
    void remove_failure() {
        // given
        // when
        when(reviewRepository.findById(2L)).thenReturn(Mono.empty());

        StepVerifier.create(reviewService.remove(2L))
                .expectErrorMatches(throwable -> throwable instanceof GlobalException &&
                        ((GlobalException) throwable).getErrorCode() == ErrorCode.REVIEW_NOT_FOUND)
                .verify();

        // then
        verify(reviewRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("상품이 속한 리뷰 목록 조회 테스트")
    void reviewsByProduct() {
        // given
        int page = 1;
        int size = 20;

        ReviewDTO.ReviewResp reviewResp = ReviewDTO.ReviewResp.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .description(review.getDescription())
                .ratings(review.getRatings())
                .reviewImgList(review.getReviewImgList())
                .writeDate(review.getWriteDate())
                .memberId(review.getMemberId())
                .writer(maskingMemberName(member.getName()))
                .build();

        PageResponseDTO<ReviewDTO.ReviewResp> pageResponseDTO = new PageResponseDTO<>(List.of(reviewResp), 1, 1, 20, true, true);

        // when
        when(reviewRepository.reviewCountByProduct(product.getProductId())).thenReturn(Mono.just(20L));
        when(reviewRepository.findAllByProductId(product.getProductId())).thenReturn(Flux.just(reviewResp));

        StepVerifier.create(reviewService.reviewsByProduct(product.getProductId(), page, size))
                .expectNextMatches(result ->
                        result.getContent().get(0).getReviewId() == pageResponseDTO.getContent().get(0).getReviewId() &&
                                result.getContent().get(0).getTitle().equals(pageResponseDTO.getContent().get(0).getTitle()) &&
                                result.getContent().get(0).getDescription().equals(pageResponseDTO.getContent().get(0).getDescription()) &&
                                result.getContent().get(0).getRatings().equals(pageResponseDTO.getContent().get(0).getRatings()) &&
                                result.getContent().get(0).getReviewImgList().size() == pageResponseDTO.getContent().get(0).getReviewImgList().size() &&
                                result.getContent().get(0).getMemberId() == pageResponseDTO.getContent().get(0).getMemberId() &&
                                result.getContent().get(0).getWriter().equals(pageResponseDTO.getContent().get(0).getWriter()) &&
                                result.getPageNumber() == page &&
                                result.getTotalPages() == pageResponseDTO.getTotalPages() &&
                                result.getTotalElements() == pageResponseDTO.getTotalElements() &&
                                result.isFirst() == pageResponseDTO.isFirst() &&
                                result.isLast() == pageResponseDTO.isLast()
                ).verifyComplete();

        // then
        verify(reviewRepository, times(1)).reviewCountByProduct(anyLong());
        verify(reviewRepository, times(1)).findAllByProductId(anyLong());
    }
}