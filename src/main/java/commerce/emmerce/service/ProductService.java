package commerce.emmerce.service;

import commerce.emmerce.config.FileHandler;
import commerce.emmerce.domain.Product;
import commerce.emmerce.domain.Review;
import commerce.emmerce.dto.*;
import commerce.emmerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

    private final FileHandler fileHandler;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 상품 추가
     * @param productReqMono
     * @param titleImg
     * @param detailImgs
     * @return
     */
    public Mono<Void> create(Mono<ProductDTO.ProductReq> productReqMono,
                             Mono<FilePart> titleImg,
                             Flux<FilePart> detailImgs) {

        return productReqMono
                .flatMap(productReq -> {
                    // 할인률 계산
                    int discount = (int) Math.round((double) (productReq.getOriginalPrice() - productReq.getDiscountPrice()) / productReq.getOriginalPrice() * 100);
                    // 이미지 임시 저장 디렉토리 위치
                    String imagePath = "C:\\emmerce\\images\\";

                    return fileHandler.saveImage(titleImg, imagePath)
                            .map(uniqueFileName -> {
                                String titleImgPath = imagePath + uniqueFileName;
                                return Tuples.of(productReq, titleImgPath);
                            })
                            .flatMap(tuple -> detailImgs.flatMap(detailImg -> fileHandler.saveImage(Mono.just(detailImg), imagePath))
                                    .collectList()
                                    .map(detailImgNames -> {
                                        List<String> detailImgPaths = detailImgNames.stream()
                                                .map(imgName -> imagePath + imgName)
                                                .collect(Collectors.toList());
                                        return Tuples.of(tuple.getT1(), tuple.getT2(), detailImgPaths);
                                    }))
                            .flatMap(tuple -> productRepository.save(Product.createProduct()
                                    .name(tuple.getT1().getName())
                                    .detail(tuple.getT1().getDetail())
                                    .originalPrice(tuple.getT1().getOriginalPrice())
                                    .discountPrice(tuple.getT1().getDiscountPrice())
                                    .discountRate(discount)
                                    .stockQuantity(tuple.getT1().getStockQuantity())
                                    .starScore(0.0) // 초기 값 세팅
                                    .titleImg(tuple.getT2())
                                    .detailImgList(tuple.getT3())
                                    .brand(tuple.getT1().getBrand())
                                    .enrollTime(LocalDateTime.now())
                                    .build()));
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
     * @param updateReqMono
     * @param titleImg
     * @param detailImgs
     * @return
     */
    public Mono<Void> update(Long productId,
                             Mono<ProductDTO.UpdateReq> updateReqMono,
                             Mono<FilePart> titleImg,
                             Flux<FilePart> detailImgs) {

        return updateReqMono.flatMap(updateReq -> {
            // 할인률 계산
            int discountRate = (int) Math.round((double) (updateReq.getOriginalPrice() - updateReq.getDiscountPrice()) / updateReq.getOriginalPrice() * 100);
            // 이미지 임시 저장 디렉토리 위치
            String imagePath = "C:\\emmerce\\images\\";

            return fileHandler.saveImage(titleImg, imagePath)
                    .map(uniqueFileName -> {
                        String titleImgPath = imagePath + uniqueFileName;
                        return Tuples.of(updateReq, titleImgPath);
                    })
                    .flatMap(tuple -> detailImgs
                            .flatMap(detailImg -> fileHandler.saveImage(Mono.just(detailImg), imagePath))
                            .collectList()
                            .map(detailImgNames -> {
                                List<String> detailImgPaths = detailImgNames.stream()
                                        .map(detailImgName -> imagePath + detailImgName)
                                        .collect(Collectors.toList());
                                return Tuples.of(tuple.getT1(), tuple.getT2(), detailImgPaths);
                            })
                    )
                    .flatMap(tuple -> productRepository.findById(productId)
                            .flatMap(product -> Mono.when(
                                    fileHandler.deleteImage(product.getTitleImg()),
                                    fileHandler.deleteImages(product.getDetailImgList())
                            ).thenReturn(product))
                            .flatMap(product -> {
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
                    ).then();
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
