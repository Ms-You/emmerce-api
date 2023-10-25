package commerce.emmerce.service;

import commerce.emmerce.domain.Product;
import commerce.emmerce.domain.Review;
import commerce.emmerce.dto.CategoryDTO;
import commerce.emmerce.dto.PageResponseDTO;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.dto.ReviewDTO;
import commerce.emmerce.repository.*;
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
    private final CategoryProductRepositoryImpl categoryProductRepository;
    private final CategoryRepositoryImpl categoryRepository;
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
                        .titleImg(productReq.getTitleImg())
                        .detailImgList(productReq.getDetailImgList())
                        .brand(productReq.getBrand())
                        .enrollTime(LocalDateTime.now())
                        .build())
                .then();
    }


    /**
     * 상품 상세 정보 반환 (with category & review)
     * @param productId
     * @return
     */
    public Mono<ProductDTO.DetailResp> detail(Long productId) {
        return customProductRepository.findDetailById(productId)
                .flatMap(detailResp -> attachCategoryInfo(detailResp, productId))
                .flatMap(detailResp -> attachReviewInfo(detailResp, productId));
    }


    /**
     * productDetailResp 에 categoryInfoResp 세팅
     * @param detailResp
     * @param productId
     * @return
     */
    private Mono<ProductDTO.DetailResp> attachCategoryInfo(ProductDTO.DetailResp detailResp, Long productId) {
        return getCategoryLayers(productId).collectList()
                .map(categoryInfoResps -> {
                    detailResp.setCategoryInfoRespList(categoryInfoResps);

                    return detailResp;
                });
    }

    /**
     * 상품이 속한 카테고리 계층 반환
     * @param productId
     * @return
     */
    public Flux<CategoryDTO.InfoResp> getCategoryLayers(Long productId) {
        return categoryProductRepository.findByProductId(productId)
                .flatMap(categoryProduct -> categoryRepository.findById(categoryProduct.getCategoryId()))
                .map(category -> CategoryDTO.InfoResp.builder()
                        .categoryId(category.getCategoryId())
                        .tier(category.getTier())
                        .name(category.getName())
                        .build()
                );
    }

    /**
     * productDetailResp 에 reviewResp 세팅
     * @param detailResp
     * @param productId
     * @return
     */
    private Mono<ProductDTO.DetailResp> attachReviewInfo(ProductDTO.DetailResp detailResp, Long productId) {
        return reviewRepository.findAllByProductId(productId)
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
                .map(reviewResps -> {
                    detailResp.setReviewRespList(reviewResps);

                    return detailResp;
                });
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
     * @param updateReq
     * @return
     */
    public Mono<Void> update(Long productId, ProductDTO.UpdateReq updateReq) {
        return customProductRepository.findById(productId)
                .flatMap(product -> {
                    // 할인률 계산
                    int discountRate = (int) Math.round((double) (updateReq.getOriginalPrice() - updateReq.getDiscountPrice()) / updateReq.getOriginalPrice() * 100);

                    product.updateProduct(updateReq.getName(), updateReq.getDetail(), updateReq.getOriginalPrice(), updateReq.getDiscountPrice(),
                            discountRate, updateReq.getStockQuantity(), updateReq.getTitleImg(), updateReq.getDetailImgList());

                    return productRepository.save(product).then();
                });
    }


    /**
     * 최신 상품 목록 조회
     * @param size
     * @return
     */
    public Flux<ProductDTO.ListResp> latest(int size) {
        return customProductRepository.findLatestProducts(size);
    }


    /**
     * 상품 검색
     * @param keyword
     * @param brand
     * @param limit
     * @param minPrice
     * @param maxPrice
     * @param page
     * @param size
     * @return
     */
    public Mono<PageResponseDTO<ProductDTO.ListResp>> search(String keyword, String brand, int limit, int minPrice, int maxPrice, int page, int size) {
        return customProductRepository.searchProductsCount(keyword, brand, limit, minPrice, maxPrice)
                .flatMap(totalElements -> customProductRepository.searchProducts(keyword, brand, limit, minPrice, maxPrice)
                        .skip((page-1) * size)
                        .take(size)
                        .collectList()
                        .map(content -> new PageResponseDTO<>(content, page, size, totalElements.intValue()))
                );
    }


    /**
     * 핫 딜 - 할인률 큰 상품 목록 조회
     * @Param size
     * @return
     */
    public Flux<ProductDTO.ListResp> hotDeal(int size) {
        return customProductRepository.findHotDealProducts(size);
    }

}
