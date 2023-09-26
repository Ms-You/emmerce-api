package commerce.emmerce.controller;

import commerce.emmerce.domain.Member;
import commerce.emmerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/member/list")
    public Flux<Member> memberList() {
        return memberRepository.findAll();
    }

}
