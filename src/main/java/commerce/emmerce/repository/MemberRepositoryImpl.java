package commerce.emmerce.repository;

import commerce.emmerce.domain.Member;
import commerce.emmerce.domain.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Repository
public class MemberRepositoryImpl {

    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;

    public Mono<Void> save(Member member) {
        String memberQuery = """
                insert into member (name, email, password, tel, birth, profile_img, point, role, city, street, zipcode) 
                values (:name, :email, :password, :tel, :birth, :profileImg, :point, :role, :city, :street, :zipcode) returning member_id
                """;

        String cartQuery = """
                insert into cart (member_id) values (:memberId)
                """;

        return databaseClient.sql(memberQuery)
                .bind("name", member.getName())
                .bind("email", member.getEmail())
                .bind("password" ,member.getPassword())
                .bind("tel", member.getTel())
                .bind("birth", member.getBirth())
                .bind("profileImg", member.getProfileImg())
                .bind("point", member.getPoint())
                .bind("role", member.getRole().name())
                .bind("city", member.getCity())
                .bind("street", member.getStreet())
                .bind("zipcode", member.getZipcode())
                .fetch().one()
                .map(result -> (Long) result.get("member_id"))
                .flatMap(memberId ->
                        databaseClient.sql(cartQuery)
                                .bind("memberId", memberId)
                                .then()
                ).switchIfEmpty(Mono.empty())
                .as(transactionalOperator::transactional);
    }


    public Mono<Member> findByName(String name) {
        String query = """
                select *
                from member m
                where m.name = :name
                """;

        return databaseClient.sql(query)
                .bind("name", name)
                .fetch().one()
                .map(row -> Member.createMember()
                        .id((Long) row.get("member_id"))
                        .name((String) row.get("name"))
                        .email((String) row.get("email"))
                        .password((String) row.get("password"))
                        .tel((String) row.get("tel"))
                        .birth((String) row.get("birth"))
                        .profileImg((String) row.get("profile_img"))
                        .point((Integer) row.get("point"))
                        .role(RoleType.valueOf((String) row.get("role")))
                        .city((String) row.get("city"))
                        .street((String) row.get("street"))
                        .zipcode((String) row.get("zipcode"))
                        .build());
    }

}
