package commerce.emmerce.controller;

import commerce.emmerce.domain.Member;
import commerce.emmerce.dto.MemberReq;
import commerce.emmerce.service.AuthService;
import lombok.RequiredArgsConstructor;
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


    @PostMapping("/auth/signup")
    public Mono<Member> signup(@RequestBody MemberReq memberReq) {
        return authService.register(memberReq);
    }


}
