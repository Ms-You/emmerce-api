package commerce.emmerce.service;

import commerce.emmerce.domain.Product;
import commerce.emmerce.domain.Review;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.dto.ReviewDTO;
import commerce.emmerce.repository.MemberRepositoryImpl;
import commerce.emmerce.repository.ProductRepository;
import commerce.emmerce.repository.ProductRepositoryImpl;
import commerce.emmerce.repository.ReviewRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepositoryImpl customProductRepository;
    private final ProductRepository productRepository;
    private final ReviewRepositoryImpl reviewRepository;
    private final MemberRepositoryImpl memberRepository;

    /**
     * 상품 추가
     * @param productReq
     * @return
     */
    public Mono<Void> create(ProductDTO.ProductReq productReq) {
        // 할인률 계산
        int discountRate = (int) Math.round((double) (productReq.getOriginalPrice() - productReq.getDiscountPrice()) / productReq.getOriginalPrice() * 100);

        return productRepository.save(Product.createProduct()
                        .name(productReq.getName())
                        .detail(productReq.getDetail())
                        .originalPrice(productReq.getOriginalPrice())
                        .discountPrice(productReq.getDiscountPrice())
                        .discountRate(discountRate)
                        .stockQuantity(productReq.getStockQuantity())
                        .starScore(0.0) // 초기 값 세팅
                        .titleImgList(productReq.getTitleImgList())
                        .detailImgList(productReq.getDetailImgList())
                        .seller(productReq.getSeller())
                        .enrollTime(LocalDateTime.now())
                        .build())
                .then();
    }


    /**
     * 상품 상세 정보 반환 (with review)
     * @param productId
     * @return
     */
    public Mono<ProductDTO.ProductDetailResp> detail(Long productId) {
        return customProductRepository.findDetailById(productId)
                .flatMap(productDetailResp -> reviewRepository.findAllByProductId(productId)
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
                                        .build()
                                )
                        ).collectList()
                        .map(reviewRespList -> {
                            productDetailResp.setReviewRespList(reviewRespList);
                            return productDetailResp;
                        })
                );
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


    /**
     * 모든 상품 별점 업데이트
     * @return
     */
    public Mono<Void> updateAllProductStarScore() {
        return customProductRepository.findAll()
                .flatMap(product -> reviewRepository.findAllByProductId(product.getProductId())
                        .collectList()
                        .filter(reviews -> !reviews.isEmpty())
                        .map(reviews -> {
                            double totalScore = 0;
                            for (Review review : reviews) {
                                totalScore += review.getStarScore();
                            }

                            double resultScore = totalScore / reviews.size();
                            resultScore = Math.round(resultScore * 10) / 10.0;  // 소수점 둘 째 자리에서 반올림
                            product.updateStarScore(resultScore);

                            return product;
                        })
                )
                .flatMap(product -> productRepository.save(product))
                .then();
    }


    /**
     * 상품 정보 수정
     * @param productId
     * @return
     */
    public Mono<Void> update(Long productId, ProductDTO.ProductUpdateReq productUpdateReq) {
        return customProductRepository.findById(productId)
                .flatMap(product -> {
                    // 할인률 계산
                    int discountRate = (int) Math.round((double) (productUpdateReq.getOriginalPrice() - productUpdateReq.getDiscountPrice()) / productUpdateReq.getOriginalPrice() * 100);

                    product.updateProduct(productUpdateReq.getName(), productUpdateReq.getDetail(), productUpdateReq.getOriginalPrice(), productUpdateReq.getDiscountPrice(),
                            discountRate, productUpdateReq.getStockQuantity(), productUpdateReq.getTitleImgList(), productUpdateReq.getDetailImgList());

                    return productRepository.save(product).then();
                });
    }


    /**
     * 최신 상품 12개 조회
     * @return
     */
    public Flux<ProductDTO.ProductListResp> latest() {
        return customProductRepository.findLatestProducts()
                .map(product -> ProductDTO.ProductListResp.builder()
                        .productId(product.getProductId())
                        .name(product.getName())
                        .originalPrice(product.getOriginalPrice())
                        .discountPrice(product.getDiscountPrice())
                        .discountRate(product.getDiscountRate())
                        .titleImgList(product.getTitleImgList())
                        .build()
                );
    }

}
