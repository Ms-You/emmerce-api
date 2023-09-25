package commerce.emmerce.service;

import commerce.emmerce.domain.Member;
import commerce.emmerce.domain.RoleType;
import commerce.emmerce.dto.MemberReq;
import commerce.emmerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public Mono<Member> register(MemberReq memberReq) {
        passwordCorrect(memberReq.getPassword(), memberReq.getPasswordConfirm());

        Member member = Member.createMember()
                .name(memberReq.getName())
                .email(memberReq.getEmail())
                .password(passwordEncoder.encode(memberReq.getPassword()))
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


    private boolean passwordCorrect(String password, String passwordConfirm) {
        // 추후 예외 처리
        return password.equals(passwordConfirm) ? true : false;
    }

}
