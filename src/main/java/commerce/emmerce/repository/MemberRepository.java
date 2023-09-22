package commerce.emmerce.repository;

import commerce.emmerce.domain.Member;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends R2dbcRepository<Member, Long> {
}
