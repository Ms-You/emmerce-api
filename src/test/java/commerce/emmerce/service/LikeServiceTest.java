package commerce.emmerce.service;

import commerce.emmerce.domain.Like;
import commerce.emmerce.domain.Member;
import commerce.emmerce.domain.RoleType;
import commerce.emmerce.repository.LikeRepository;
import commerce.emmerce.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import(LikeService.class)
class LikeServiceTest {
    @Autowired
    private LikeService likeService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private LikeRepository likeRepository;

    private Member member;
    private Like like;
    private SecurityContext securityContext;

    @BeforeEach
    void setup() {
        member = Member.createMember()
                .id(1L)
                .name("testId001")
                .email("test@test.com")
                .password("password")
                .tel("01012345678")
                .birth("240422")
                .point(0)
                .role(RoleType.ROLE_USER)
                .city("서울특별시")
                .street("공룡로 50")
                .zipcode("18888")
                .build();

        like = Like.builder()
                .likeId(1L)
                .memberId(member.getMemberId())
                .productId(1L)
                .build();

        when(memberRepository.findByName(member.getName())).thenReturn(Mono.just(member));

        Authentication authentication = mock(Authentication.class);
        securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(member.getName());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(securityContext.getAuthentication().getName()).thenReturn(member.getName());
    }


    @Test
    @DisplayName("상품 좋아요 테스트 - 이미 좋아요 누른 상태")
    void toggleLike_already() {
        // given
        // when
        when(likeRepository.findByMemberIdAndProductId(member.getMemberId(), like.getProductId())).thenReturn(Mono.just(like));
        when(likeRepository.deleteByMemberIdAndProductId(member.getMemberId(), like.getProductId())).thenReturn(Mono.empty());

        StepVerifier.create(likeService.toggleLike(like.getProductId())
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        ).verifyComplete();

        // then
        verify(memberRepository, times(1)).findByName(anyString());
        verify(likeRepository, times(1)).findByMemberIdAndProductId(anyLong(), anyLong());
        verify(likeRepository, times(1)).deleteByMemberIdAndProductId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("상품 좋아요 테스트 - 아직 안누른 상태")
    void toggleLike_not_yet() {
        // given
        // when
        when(likeRepository.findByMemberIdAndProductId(member.getMemberId(), like.getProductId())).thenReturn(Mono.empty());
        when(likeRepository.save(any(Like.class))).thenReturn(Mono.empty());

        StepVerifier.create(likeService.toggleLike(like.getProductId())
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        ).verifyComplete();

        // then
        verify(memberRepository, times(1)).findByName(anyString());
        verify(likeRepository, times(1)).findByMemberIdAndProductId(anyLong(), anyLong());
        verify(likeRepository, times(1)).save(any(Like.class));
    }
}