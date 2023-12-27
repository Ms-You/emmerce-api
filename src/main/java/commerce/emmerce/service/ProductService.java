package commerce.emmerce.service;

import commerce.emmerce.config.s3.S3FileUploader;
import commerce.emmerce.domain.Product;
import commerce.emmerce.dto.*;
import commerce.emmerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

    private final S3FileUploader s3FileUploader;
    private final ProductRepository productRepository;

    /**
     * 상품 추가
     * @param productReqMono
     * @param titleImage
     * @param detailImages
     * @return
     */
    public Mono<Void> create(Mono<ProductDTO.ProductReq> productReqMono,
                             Mono<FilePart> titleImage,
                             Flux<FilePart> detailImages) {
        return productReqMono.zipWith(s3FileUploader.uploadS3Image(titleImage, "product/title"))  // 타이틀 이미지 업로드
                .flatMap(tuple -> s3FileUploader.uploadS3ImageList(detailImages, "product/detail") // 디테일 이미지 목록 업로드
                        .map(savedImagePaths -> Tuples.of(tuple.getT1(), tuple.getT2(), savedImagePaths)))
                .flatMap(tuple -> {
                    // 할인률 계산
                    int discountRate = 0;
                    if(tuple.getT1().getOriginalPrice() != 0) {
                        discountRate = (int) Math.round((double) (tuple.getT1().getOriginalPrice() - tuple.getT1().getDiscountPrice()) / tuple.getT1().getOriginalPrice() * 100);
                    }
                    return productRepository.save(Product.createProduct()
                            .name(tuple.getT1().getName())
                            .detail(tuple.getT1().getDetail())
                            .originalPrice(tuple.getT1().getOriginalPrice())
                            .discountPrice(tuple.getT1().getDiscountPrice())
                            .discountRate(discountRate)
                            .stockQuantity(tuple.getT1().getStockQuantity())
                            .starScore(0.0) // 초기 값 세팅
                            .totalReviews(0)
                            .titleImg(tuple.getT2())
                            .detailImgList(tuple.getT3())
                            .brand(tuple.getT1().getBrand())
                            .enrollTime(LocalDateTime.now())
                            .build());
                });
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
     * 상품 정보 수정
     * @param productId
     * @param updateReqMono
     * @param titleImage
     * @param detailImages
     * @return
     */
    public Mono<Void> update(Long productId,
                             Mono<ProductDTO.UpdateReq> updateReqMono,
                             Mono<FilePart> titleImage,
                             Flux<FilePart> detailImages) {
        return updateReqMono.zipWith(s3FileUploader.uploadS3Image(titleImage, "product/title"))
                .flatMap(tuple -> s3FileUploader.uploadS3ImageList(detailImages, "product/detail")
                        .map(savedImageNames -> Tuples.of(tuple.getT1(), tuple.getT2(), savedImageNames))
                ).flatMap(tuple -> productRepository.findById(productId)
                        .flatMap(product -> Mono.when(
                                s3FileUploader.deleteS3Image(product.getTitleImg(), "product/title"),
                                s3FileUploader.deleteS3ImageList(product.getDetailImgList(), "product/detail")
                        ).thenReturn(product))
                        .flatMap(product -> {
                            int discountRate = 0;
                            if(tuple.getT1().getOriginalPrice() != 0) {
                                discountRate = (int) Math.round((double) (tuple.getT1().getOriginalPrice() - tuple.getT1().getDiscountPrice()) / tuple.getT1().getOriginalPrice() * 100);
                            }
                            product.updateProduct(
                                    tuple.getT1().getName(),
                                    tuple.getT1().getDetail(),
                                    tuple.getT1().getOriginalPrice(),
                                    tuple.getT1().getDiscountPrice(),
                                    discountRate,
                                    tuple.getT1().getStockQuantity(),
                                    tuple.getT2(),
                                    tuple.getT3()
                            );
                            return productRepository.save(product);
                        })
                );
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
