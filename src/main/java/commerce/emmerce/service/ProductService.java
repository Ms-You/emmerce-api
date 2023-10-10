package commerce.emmerce.service;

import commerce.emmerce.domain.Product;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.repository.ProductRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepositoryImpl productRepository;

    public Mono<Void> create(ProductDTO.ProductReq productReq) {
        Product product = Product.createProduct()
                .name(productReq.getName())
                .detail(productReq.getDetail())
                .originalPrice(productReq.getOriginalPrice())
                .discountPrice(productReq.getDiscountPrice())
                .discountRate(productReq.getDiscountRate())
                .stockQuantity(productReq.getStockQuantity())
                .starScore(productReq.getStarScore())
                .titleImgList(productReq.getTitleImgList())
                .detailImgList(productReq.getDetailImgList())
                .seller(productReq.getSeller())
                .build();

        return productRepository.save(product);
    }


    public Mono<ProductDTO.ProductDetailResp> detail(Long productId) {
        return productRepository.findDetailById(productId);
    }

}
