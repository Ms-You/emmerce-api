package commerce.emmerce.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import commerce.emmerce.config.exception.GlobalErrorAttributes;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.dto.ProductDTO;
import commerce.emmerce.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser
@WebFluxTest(AdminProductController.class)
class AdminProductControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("상품 추가 테스트")
    void createProduct() throws JsonProcessingException {
        // given
        ProductDTO.ProductReq productReq = ProductDTO.ProductReq.builder()
                .name("샴푸")
                .detail("머리가 자라나는 샴푸")
                .originalPrice(10000)
                .discountPrice(8000)
                .stockQuantity(100)
                .brand("에비앙")
                .build();

        String productReqJson = objectMapper.writeValueAsString(productReq);

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("productReq", productReqJson, MediaType.APPLICATION_JSON);

        Resource titleResource = new ByteArrayResource("bytes".getBytes()) {
            @Override
            public String getFilename() {
                return "titleImage.jpg";
            }
        };

        Resource detailResource = new ByteArrayResource("bytes".getBytes()) {
            @Override
            public String getFilename() {
                return "detailImage.jpg";
            }
        };

        bodyBuilder.part("titleImage", titleResource);
        bodyBuilder.part("detailImages", detailResource);

        // when
        when(productService.create(any(Mono.class), any(Mono.class), any(Flux.class))).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/admin/product")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(bodyBuilder.build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(productService).create(any(Mono.class), any(Mono.class), any(Flux.class));
    }

    @Test
    @DisplayName("상품 정보 수정 테스트")
    void updateProduct() throws JsonProcessingException {
        // given
        Long productId = 1L;

        ProductDTO.UpdateReq updateReq = ProductDTO.UpdateReq.builder()
                .name("샴푸")
                .detail("머리가 자라나는 샴푸")
                .originalPrice(10000)
                .discountPrice(8000)
                .stockQuantity(100)
                .build();

        String updateReqJson = objectMapper.writeValueAsString(updateReq);

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("updateReq", updateReqJson, MediaType.APPLICATION_JSON);

        Resource titleResource = new ByteArrayResource("bytes".getBytes()) {
            @Override
            public String getFilename() {
                return "titleImage.jpg";
            }
        };

        Resource detailImages = new ByteArrayResource("bytes".getBytes()) {
            @Override
            public String getFilename() {
                return "detailImages.jpg";
            }
        };

        bodyBuilder.part("titleImage", titleResource);
        bodyBuilder.part("detailImages", detailImages);

        // when
        when(productService.update(anyLong(), any(Mono.class), any(Mono.class), any(Flux.class))).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf())
                .put()
                .uri("/admin/product/{productId}", productId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(bodyBuilder.build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(productService).update(anyLong(), any(Mono.class), any(Mono.class), any(Flux.class));
    }
}