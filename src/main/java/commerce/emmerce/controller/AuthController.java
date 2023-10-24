package commerce.emmerce.controller;

import commerce.emmerce.dto.MemberDTO;
import commerce.emmerce.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Auth", description = "사용자 인증 관련 API")
@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * @param registerReq
     * @return
     */
    @Operation(summary = "회원가입", description = "필요한 정보를 받아 회원가입을 진행합니다.\n비밀번호 확인 일치 여부도 체크합니다.")
    @Parameter(name = "registerReq", description = "회원가입 시 필요한 데이터")
    @PostMapping("/register")
    public Mono<Void> signup(@RequestBody MemberDTO.RegisterReq registerReq) {
        return authService.register(registerReq);
    }


    /**
     * 사용자 이름 중복 체크
     * @param duplicateCheckReq
     * @return
     */
    @PostMapping("/duplicate-check")
    public Mono<Void> duplicateCheckName(@RequestBody MemberDTO.DuplicateCheckReq duplicateCheckReq) {
        return authService.duplicateCheck(duplicateCheckReq);
    }


    /**
     * 로그인
     * @param loginReq
     * @return
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@RequestBody MemberDTO.LoginReq loginReq) {
        return authService.login(loginReq)
                .map(tokenDTO -> {
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add("Authorization", "Bearer " + tokenDTO.getAccessToken());
                    httpHeaders.add("RefreshToken", tokenDTO.getAccessToken());

                    return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
                });

    }

}
