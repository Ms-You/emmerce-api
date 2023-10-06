package commerce.emmerce.controller;

import commerce.emmerce.domain.Member;
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


    @PostMapping("/register")
    public Mono<Void> signup(@RequestBody MemberDTO.MemberRegisterReq memberRegisterReq) {
        return authService.register(memberRegisterReq);
    }


    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@RequestBody MemberDTO.MemberLoginReq memberLoginReq) {
        return authService.login(memberLoginReq)
                .map(tokenDTO -> {
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add("Authorization", "Bearer " + tokenDTO.getAccessToken());
                    httpHeaders.add("RefreshToken", tokenDTO.getAccessToken());

                    return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
                });

    }

}
