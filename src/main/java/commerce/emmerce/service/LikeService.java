package commerce.emmerce.service;

import commerce.emmerce.config.SecurityUtil;
import commerce.emmerce.domain.Like;
import commerce.emmerce.domain.Member;
import commerce.emmerce.repository.LikeRepository;
import commerce.emmerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;

    private Mono<Member> findCurrentMember() {
        return SecurityUtil.getCurrentMemberName()
                .flatMap(name -> memberRepository.findByName(name));
    }

    /**
     * 상품 좋아요 (토글)
     * @param productId
     * @return
     */
    public Mono<Void> toggleLike(Long productId) {
        return findCurrentMember()
                .flatMap(member -> likeRepository.findByMemberIdAndProductId(member.getMemberId(), productId)
                        .hasElement()
                        .flatMap(exists -> {
                            if (exists) {
                                return likeRepository.deleteByMemberIdAndProductId(member.getMemberId(), productId);
                            } else {
                                return likeRepository.save(Like.builder()
                                        .memberId(member.getMemberId())
                                        .productId(productId)
                                        .build()).then();
                            }
                        })
                );
    }

}
