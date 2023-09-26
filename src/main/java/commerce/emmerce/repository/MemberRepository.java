package commerce.emmerce.repository;

import commerce.emmerce.domain.Member;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MemberRepository extends R2dbcRepository<Member, Long> {
    @Query("select * from member m where m.name = :name")
    Mono<Member> findByName(String name);
}
