package commerce.emmerce.service;

import commerce.emmerce.domain.Product;
import commerce.emmerce.domain.Review;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.repository.ProductRepositoryImpl;
import commerce.emmerce.repository.ReviewRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepositoryImpl productRepository;
    private final ReviewRepositoryImpl reviewRepository;

    public Mono<Void> create(ProductDTO.ProductReq productReq) {
        Product product = Product.createProduct()
                .name(productReq.getName())
                .detail(productReq.getDetail())
                .originalPrice(productReq.getOriginalPrice())
                .discountPrice(productReq.getDiscountPrice())
                .discountRate(productReq.getDiscountRate())
                .stockQuantity(productReq.getStockQuantity())
                .starScore(0.0) // 초기 값 세팅
                .titleImgList(productReq.getTitleImgList())
                .detailImgList(productReq.getDetailImgList())
                .seller(productReq.getSeller())
                .build();

        return productRepository.save(product);
    }


    public Mono<ProductDTO.ProductDetailResp> detail(Long productId) {
        return productRepository.findDetailById(productId);
    }


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

}
