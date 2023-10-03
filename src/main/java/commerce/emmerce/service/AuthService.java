package commerce.emmerce.service;

import commerce.emmerce.config.jwt.TokenDTO;
import commerce.emmerce.config.jwt.TokenProvider;
import commerce.emmerce.domain.Member;
import commerce.emmerce.domain.RoleType;
import commerce.emmerce.dto.MemberDTO;
import commerce.emmerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final ReactiveAuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    public Mono<Member> register(MemberDTO.MemberRegisterReq memberRegisterReq) {
        passwordCorrect(memberRegisterReq.getPassword(), memberRegisterReq.getPasswordConfirm());

        Member member = Member.createMember()
                .name(memberRegisterReq.getName())
                .email(memberRegisterReq.getEmail())
                .password(passwordEncoder.encode(memberRegisterReq.getPassword()))
                .tel(memberRegisterReq.getTel())
                .birth(memberRegisterReq.getBirth())
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


    public Mono<TokenDTO> login(MemberDTO.MemberLoginReq memberLoginReq) {
        Authentication authentication =
        new UsernamePasswordAuthenticationToken(memberLoginReq.getLoginId(), memberLoginReq.getPassword());

        return authenticationManager.authenticate(authentication)
                .map(tokenProvider::generateToken)
                .map(token -> TokenDTO.builder().accessToken(token.getAccessToken()).refreshToken(token.getRefreshToken()).build());
    }

}
