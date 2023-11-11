package commerce.emmerce.service;

import commerce.emmerce.domain.Product;
import commerce.emmerce.domain.Review;
import commerce.emmerce.dto.*;
import commerce.emmerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

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
                        .build());
    }

    /**
     * 상품 상세 정보 반환
     * @param productId
     * @return
     */
    public Mono<ProductDTO.DetailResp> detail(Long productId) {
        return productRepository.findDetailById(productId);
    }

    /**
     * 모든 상품 별점 업데이트
     * @return
     */
    public Mono<Void> updateAllProductStarScore() {
        return productRepository.findAll()
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
        return productRepository.findById(productId)
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
    public Flux<ProductDTO.ListResp> latest(Integer size) {
        return productRepository.findLatestProducts(size);
    }

    /**
     * 상품 검색
     * @param searchParamDTO
     * @return
     */
    public Mono<PageResponseDTO<ProductDTO.ListResp>> search(SearchParamDTO searchParamDTO) {
        int page = searchParamDTO.getPage();
        int size = searchParamDTO.getSize();

        return productRepository.searchProductsCount(searchParamDTO)
                .flatMap(totalElements -> productRepository.searchProducts(searchParamDTO)
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
    public Flux<ProductDTO.ListResp> hotDeal(Integer size) {
        return productRepository.findHotDealProducts(size);
    }

    /**
     * 랭킹 - 많이 팔린 상품 목록 조회
     * @param size
     * @return
     */
    public Flux<ProductDTO.ListResp> ranking(Integer size) {
        return productRepository.findRankingProducts(size);
    }

}
