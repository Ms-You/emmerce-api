package commerce.emmerce.controller;

import commerce.emmerce.config.exception.GlobalErrorAttributes;
import commerce.emmerce.config.jwt.TokenDTO;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.dto.MemberDTO;
import commerce.emmerce.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser
@WebFluxTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    @MockBean
    private GlobalErrorAttributes globalErrorAttributes;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    @DisplayName("회원가입 테스트")
    void signup() {
        // given
        MemberDTO.RegisterReq registerReq = new MemberDTO.RegisterReq("tester001", "test@test.com",
                "password", "password", "01012345678", "240422");

        // when
        webTestClient.mutateWith(csrf())
                .post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(authService).register(any(MemberDTO.RegisterReq.class));
    }

    @Test
    @DisplayName("닉네임 중복 테스트")
    void duplicateCheckName() {
        // given
        MemberDTO.DuplicateCheckReq duplicateCheckReq = new MemberDTO.DuplicateCheckReq("tester001");

        // when
        webTestClient.mutateWith(csrf())
                .post()
                .uri("/auth/duplicate-check")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(duplicateCheckReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);

        // then
        verify(authService).duplicateCheck(any(MemberDTO.DuplicateCheckReq.class));
    }

    @Test
    @DisplayName("로그인 테스트")
    void login() {
        // given
        MemberDTO.LoginReq loginReq = new MemberDTO.LoginReq("tester001", "password");
        TokenDTO tokenDTO = new TokenDTO("AccessToken", "RefreshToken");

        // when
        when(authService.login(Mockito.any(MemberDTO.LoginReq.class))).thenReturn(Mono.just(tokenDTO));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginReq)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Authorization")
                .expectHeader().valueEquals("Authorization", "Bearer " + tokenDTO.getAccessToken())
                .expectHeader().valueEquals("RefreshToken", tokenDTO.getRefreshToken());

        // then
        verify(authService).login(any(MemberDTO.LoginReq.class));
    }

    @Test
    @DisplayName("로그아웃 테스트")
    void logout() {
        // given
        String accessToken = "AccessToken";

        // when
        when(authService.logout(anyString())).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/auth/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseEntity.class);

        // then
        verify(authService).logout(accessToken);
    }

    @Test
    @DisplayName("토큰 재발급 테스트")
    void reissueToken() {
        // given
        String accessToken = "AccessToken";
        String refreshToken = "RefreshToken";
        TokenDTO tokenDTO = new TokenDTO(accessToken, refreshToken);

        // when
        when(authService.reissue(anyString(), anyString())).thenReturn(Mono.just(tokenDTO));

        webTestClient.mutateWith(csrf())
                .post()
                .uri("/auth/reissue")
                .header("Authorization", "Bearer " + accessToken)
                .header("RefreshToken", refreshToken)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Authorization")
                .expectHeader().valueEquals("Authorization", "Bearer " + tokenDTO.getAccessToken())
                .expectHeader().valueEquals("RefreshToken", tokenDTO.getRefreshToken());

        // then
        verify(authService).reissue(accessToken, refreshToken);
    }
}