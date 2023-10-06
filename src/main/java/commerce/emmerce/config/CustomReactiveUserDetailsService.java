package commerce.emmerce.config;

import commerce.emmerce.domain.Member;
import commerce.emmerce.repository.MemberRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {

    private final MemberRepositoryImpl memberRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return this.memberRepository.findByName(username)
                .map(user -> User.withUsername(user.getName())
                        .password(user.getPassword())
                        .authorities(getAuthorities(user))
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build());
    }

    private Collection<GrantedAuthority> getAuthorities(Member member){
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(member.getRole().toString()));

        return authorities;
    }
}
