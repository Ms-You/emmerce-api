package commerce.emmerce.controller;

import commerce.emmerce.domain.Member;
import commerce.emmerce.domain.RoleType;
import commerce.emmerce.dto.MemberReq;
import commerce.emmerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberRepository memberRepository;

    @PostMapping("/member/signup")
    public Mono<Member> signup(@RequestBody MemberReq memberReq) {
        Member member = Member.createMember()
                .name(memberReq.getName())
                .email(memberReq.getEmail())
                .password(memberReq.getPassword())
                .tel(memberReq.getTel())
                .birth(memberReq.getBirth())
                .profileImg(null)
                .point(0)
                .role(RoleType.ROLE_USER)
                .city("도시")
                .street("도로명")
                .zipcode("우편번호")
                .build();

        return memberRepository.save(member);
    }

    @GetMapping("/member/list")
    public Flux<Member> memberList() {
        return memberRepository.findAll();
    }

}
