package commerce.emmerce.controller;

import commerce.emmerce.dto.MemberDTO;
import commerce.emmerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
