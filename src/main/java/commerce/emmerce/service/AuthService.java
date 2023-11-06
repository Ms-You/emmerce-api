package commerce.emmerce.service;

import commerce.emmerce.config.exception.ErrorCode;
import commerce.emmerce.config.exception.GlobalException;
import commerce.emmerce.config.jwt.TokenDTO;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.domain.Member;
import commerce.emmerce.domain.RoleType;
import commerce.emmerce.dto.MemberDTO;
import commerce.emmerce.repository.MemberRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final ReactiveAuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final MemberRepositoryImpl memberRepository;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Value("${jwt.live.rtk}")
    private long refreshTokenExpiresIn;

    /**
     * 회원가입
     * @param registerReq
     * @return
     */
    public Mono<Void> register(MemberDTO.RegisterReq registerReq) {
        passwordCorrect(registerReq.getPassword(), registerReq.getPasswordConfirm());

        Member member = Member.createMember()
                .name(registerReq.getName())
                .email(registerReq.getEmail())
                .password(passwordEncoder.encode(registerReq.getPassword()))
                .tel(registerReq.getTel())
                .birth(registerReq.getBirth())
                .profileImg("default_img")
                .point(0)
                .role(RoleType.ROLE_USER)
                .city("도시")
                .street("도로명")
                .zipcode("우편번호")
                .build();

        return memberRepository.save(member);
    }


    /**
     * 사용자 이름 중복 체크
     * @param duplicateCheckReq
     * @return
     */
    public Mono<Void> duplicateCheck(MemberDTO.DuplicateCheckReq duplicateCheckReq) {
        return memberRepository.findByName(duplicateCheckReq.getName())
                .flatMap(existingMember ->
                        Mono.error(new GlobalException(ErrorCode.NAME_ALREADY_EXIST))
                );
    }


    /**
     * 비밀번호 일치 여부 확인
     * @param password
     * @param passwordConfirm
     * @return
     */
    private void passwordCorrect(String password, String passwordConfirm) {
        if(!password.equals(passwordConfirm))
            throw new GlobalException(ErrorCode.PASSWORD_NOT_MATCH);
    }


    /**
     * 로그인
     * @param loginReq
     * @return
     */
    public Mono<TokenDTO> login(MemberDTO.LoginReq loginReq) {
        Authentication authentication =
        new UsernamePasswordAuthenticationToken(loginReq.getName(), loginReq.getPassword());

        return authenticationManager.authenticate(authentication)
                .map(tokenProvider::generateToken)
                .flatMap(token -> {
                    TokenDTO tokenDTO = TokenDTO.builder()
                            .accessToken(token.getAccessToken())
                            .refreshToken(token.getRefreshToken())
                            .build();

                    return reactiveRedisTemplate.opsForValue().set(
                            authentication.getName(),
                            tokenDTO.getRefreshToken(),
                            Duration.ofMillis(refreshTokenExpiresIn)
                    ).thenReturn(tokenDTO);
                });
    }


    /**
     * 로그아웃 (jwt token redis 에서 관리)
     * @param token
     * @return
     */
    public Mono<Void> logout(String token) {
        return tokenProvider.validateToken(token)
                .flatMap(valid -> {
                    if(!valid) {
                        return Mono.error(new GlobalException(ErrorCode.ACCESS_TOKEN_NOT_VALIDATE));
                    }

                    return tokenProvider.getAuthentication(token);
                }).flatMap(authentication -> reactiveRedisTemplate.opsForValue().get(authentication.getName())
                        .flatMap(refreshToken -> reactiveRedisTemplate.delete(authentication.getName()) // refreshToken 은 삭제
                                .then(reactiveRedisTemplate.opsForValue().set(  // accessToken 은 블랙리스트로 관리
                                        token,
                                        "logout",
                                        Duration.ofMillis(tokenProvider.getExpiration(token))
                                ))
                        )
                ).then();
    }


    /**
     * 토큰 만료 시 재발행
     * @param accessToken
     * @param refreshToken
     * @return
     */
    public Mono<TokenDTO> reissue(String accessToken, String refreshToken) {
        return tokenProvider.validateToken(refreshToken)
                .flatMap(valid -> {
                    if(!valid) {
                        return Mono.error(new GlobalException(ErrorCode.REFRESH_TOKEN_NOT_VALIDATE));
                    }

                    return tokenProvider.getAuthentication(accessToken);
                }).flatMap(authentication -> reactiveRedisTemplate.opsForValue().get(authentication.getName())
                        .flatMap(savedRefreshToken -> {
                            if(!savedRefreshToken.equals(refreshToken)) {
                                return Mono.error(new GlobalException(ErrorCode.REFRESH_TOKEN_NOT_MATCHED));
                            }

                            TokenDTO tokenDTO = tokenProvider.generateToken(authentication);

                            return reactiveRedisTemplate.opsForValue().set(
                                    authentication.getName(),
                                    tokenDTO.getRefreshToken(),
                                    Duration.ofMillis(refreshTokenExpiresIn)
                            ).thenReturn(tokenDTO);
                        })
                );
    }

}
