package commerce.emmerce.service;

import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.config.jwt.TokenDTO;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.domain.Member;
import commerce.emmerce.dto.MemberDTO;
import commerce.emmerce.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(AuthService.class)
@TestPropertySource(properties = {"jwt.live.rtk=604800000"})
class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private ReactiveAuthenticationManager authenticationManager;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @MockBean
    private ReactiveValueOperations<String, String> reactiveValueOperations;

    @Value("${jwt.live.rtk}")
    private long refreshTokenExpiresIn;


    @BeforeEach
    void setup() {
        when(authenticationManager.authenticate(any())).thenReturn(Mono.just(new TestingAuthenticationToken("user", "password", "ROLE_USER")));

        reactiveValueOperations = mock(ReactiveValueOperations.class);
        when(reactiveRedisTemplate.opsForValue()).thenReturn(reactiveValueOperations);
        when(reactiveValueOperations.set(anyString(), anyString(), any(Duration.class))).thenReturn(Mono.just(true));
    }


    @Test
    @DisplayName("회원가입 테스트")
    void register() {
        // given
        MemberDTO.RegisterReq registerReq = new MemberDTO.RegisterReq("testId001", "test@test.com",
                "password", "password", "01012345678", "240422");

        // when
        authService.register(registerReq);

        // then
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("로그인 아이디 중복 체크 테스트 - 성공")
    void duplicateCheck_success() {
        // given
        String name = "testId001";
        MemberDTO.DuplicateCheckReq duplicateCheckReq =
                new MemberDTO.DuplicateCheckReq("testId001");

        // when
        when(memberRepository.findByName(name)).thenReturn(Mono.empty());

        StepVerifier.create(authService.duplicateCheck(duplicateCheckReq))
                .verifyComplete();

        // then
        verify(memberRepository, times(1)).findByName(name);
    }

    @Test
    @DisplayName("로그인 아이디 중복 체크 테스트 - 실패")
    void duplicateCheck_failure() {
        // given
        String name = "testId001";
        MemberDTO.DuplicateCheckReq duplicateCheckReq =
                new MemberDTO.DuplicateCheckReq("testId001");

        // when
        when(memberRepository.findByName(name)).thenReturn(Mono.just(Member.createMember().build()));

        StepVerifier.create(authService.duplicateCheck(duplicateCheckReq))
                .expectErrorMatches(throwable -> throwable instanceof GlobalException
                        && ((GlobalException) throwable).getErrorCode() == ErrorCode.NAME_ALREADY_EXIST)
                .verify();

        // then
        verify(memberRepository, times(1)).findByName(name);
    }


    @Test
    @DisplayName("로그인 테스트")
    void login() {
        // given
        MemberDTO.LoginReq loginReq = new MemberDTO.LoginReq("testId001", "password");

        String accessToken = "AccessToken";
        String refreshToken = "RefreshToken";
        TokenDTO tokenDTO = new TokenDTO(accessToken, refreshToken);

        Authentication authentication = mock(Authentication.class);

        // when
        when(authenticationManager.authenticate(any())).thenReturn(Mono.just(authentication));
        when(authentication.getName()).thenReturn(loginReq.getName());
        when(tokenProvider.generateToken(authentication)).thenReturn(tokenDTO);
        when(tokenProvider.getExpiration(tokenDTO.getRefreshToken())).thenReturn(refreshTokenExpiresIn);

        StepVerifier.create(authService.login(loginReq))
                .expectNextMatches(token -> token.getAccessToken().equals(tokenDTO.getAccessToken())
                        && token.getRefreshToken().equals(tokenDTO.getRefreshToken())
                ).verifyComplete();

        // then
        verify(reactiveValueOperations, times(1)).set(eq(authentication.getName()), eq(tokenDTO.getRefreshToken()), any(Duration.class));
    }

    @Test
    @DisplayName("로그아웃 테스트 - 성공")
    void logout_success() {
        // given
        String name = "testId001";
        String token = "validToken";

        Authentication authentication = mock(Authentication.class);

        // when
        when(tokenProvider.validateToken(token)).thenReturn(Mono.just(true));
        when(authentication.getName()).thenReturn(name);
        when(reactiveRedisTemplate.opsForValue().get(authentication.getName())).thenReturn(Mono.just(name));
        when(reactiveRedisTemplate.delete(anyString())).thenReturn(Mono.empty());
        when(tokenProvider.getAuthentication(token)).thenReturn(Mono.just(authentication));

        StepVerifier.create(authService.logout(token))
                .verifyComplete();

        // then
        verify(reactiveRedisTemplate, times(1)).delete(anyString());
        verify(reactiveValueOperations, times(1)).set(eq(token), eq("logout"), any(Duration.class));
    }

    @Test
    @DisplayName("로그아웃 테스트 - 실패")
    void logout_failure() {
        // given
        String token = "AccessToken";

        // when
        when(tokenProvider.validateToken(token)).thenReturn(Mono.just(false));

        StepVerifier.create(authService.logout(token))
                .expectErrorMatches(throwable -> throwable instanceof GlobalException
                        && ((GlobalException) throwable).getErrorCode() == ErrorCode.ACCESS_TOKEN_NOT_VALIDATE)
                .verify();

        // then
    }

    @Test
    @DisplayName("토큰 재발급 테스트 - 성공")
    void reissue_success() {
        // given
        String name = "testId001";
        String accessToken = "AccessToken";
        String refreshToken = "RefreshToken";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        TokenDTO reissuedTokenDTO = new TokenDTO(newAccessToken, newRefreshToken);
        Authentication authentication = mock(Authentication.class);

        // when
        when(tokenProvider.validateToken(refreshToken)).thenReturn(Mono.just(true));
        when(authentication.getName()).thenReturn(name);
        when(reactiveRedisTemplate.opsForValue().get(authentication.getName())).thenReturn(Mono.just(refreshToken));
        when(tokenProvider.getAuthentication(accessToken)).thenReturn(Mono.just(authentication));
        when(tokenProvider.generateToken(authentication)).thenReturn(reissuedTokenDTO);

        StepVerifier.create(authService.reissue(accessToken, refreshToken))
                .expectNextMatches(tokenDTO -> tokenDTO.getAccessToken().equals(reissuedTokenDTO.getAccessToken())
                        && tokenDTO.getRefreshToken().equals(reissuedTokenDTO.getRefreshToken()))
                .verifyComplete();

        // then

    }

    @Test
    @DisplayName("토큰 재발급 테스트 - 리프레시 토큰 만료 실패")
    void reissue_refresh_token_expired_failure() {
        String accessToken = "AccessToken";
        String refreshToken = "RefreshToken";

        // when
        when(tokenProvider.validateToken(refreshToken)).thenReturn(Mono.just(false));

        StepVerifier.create(authService.reissue(accessToken, refreshToken))
                .expectErrorMatches(throwable -> throwable instanceof GlobalException
                        && ((GlobalException) throwable).getErrorCode() == ErrorCode.REFRESH_TOKEN_NOT_VALIDATE)
                .verify();

        // then
    }

    @Test
    @DisplayName("토큰 재발급 테스트 - 리프레시 토큰 불일치 실패")
    void reissue_refresh_token_not_matched_failure() {
        String name = "testId001";
        String accessToken = "AccessToken";
        String refreshToken = "RefreshToken";
        String savedRefreshToken = "AnotherRefreshToken";

        Authentication authentication = mock(Authentication.class);

        // when
        when(tokenProvider.validateToken(refreshToken)).thenReturn(Mono.just(true));
        when(authentication.getName()).thenReturn(name);
        when(reactiveRedisTemplate.opsForValue().get(authentication.getName())).thenReturn(Mono.just(savedRefreshToken));
        when(tokenProvider.getAuthentication(accessToken)).thenReturn(Mono.just(authentication));

        StepVerifier.create(authService.reissue(accessToken, refreshToken))
                .expectErrorMatches(throwable -> throwable instanceof GlobalException
                        && ((GlobalException) throwable).getErrorCode() == ErrorCode.REFRESH_TOKEN_NOT_MATCHED)
                .verify();

        // then
        verify(reactiveRedisTemplate.opsForValue(), never()).set(anyString(), anyString(), any());
    }

}