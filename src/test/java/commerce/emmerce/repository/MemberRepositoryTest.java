package commerce.emmerce.repository;

import commerce.emmerce.domain.Member;
import commerce.emmerce.domain.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataR2dbcTest
class MemberRepositoryTest {
    private MemberRepository memberRepository;
    private DatabaseClient databaseClient;
    private TransactionalOperator transactionalOperator;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private FetchSpec<Map<String, Object>> fetchSpec;

    private Member member;

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

        databaseClient = mock(DatabaseClient.class);
        transactionalOperator = mock(TransactionalOperator.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        fetchSpec = mock(FetchSpec.class);

        // transactional 메서드가 호출될 때마다 인자로 전해지는 Mono 객체를 그대로 반환하도록 함
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);

        memberRepository = new MemberRepository(databaseClient, transactionalOperator);
    }

    @Test
    void save() {
        // given

        // when
        when(fetchSpec.one()).thenReturn(Mono.just(Map.of("member_id", member.getMemberId())));
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(memberRepository.save(member))
                .verifyComplete();

        // then
        verify(databaseClient, atLeastOnce()).sql(anyString());
    }

    @Test
    void findByName() {
        // given
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("member_id", 1L);
        dataMap.put("name", "testId001");
        dataMap.put("email", "test@test.com");
        dataMap.put("password", "password");
        dataMap.put("tel", "01012345678");
        dataMap.put("birth", "240422");
        dataMap.put("point", 0);
        dataMap.put("role", "ROLE_USER");
        dataMap.put("city", "서울특별시");
        dataMap.put("street", "공룡로 50");
        dataMap.put("zipcode", "18888");

        // when
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(memberRepository.findByName(member.getName()))
                .expectNextMatches(result ->
                        result.getMemberId() == member.getMemberId() &&
                                result.getName().equals(member.getName()) &&
                                result.getEmail().equals(member.getEmail()) &&
                                result.getPassword().equals(member.getPassword()) &&
                                result.getTel().equals(member.getTel()) &&
                                result.getBirth().equals(member.getBirth()) &&
                                result.getPoint() == member.getPoint() &&
                                result.getRole().equals(member.getRole()) &&
                                result.getCity().equals(member.getCity()) &&
                                result.getStreet().equals(member.getStreet()) &&
                                result.getZipcode().equals(member.getZipcode())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
    }

    @Test
    void findById() {
        // given
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("member_id", 1L);
        dataMap.put("name", "testId001");
        dataMap.put("email", "test@test.com");
        dataMap.put("password", "password");
        dataMap.put("tel", "01012345678");
        dataMap.put("birth", "240422");
        dataMap.put("point", 0);
        dataMap.put("role", "ROLE_USER");
        dataMap.put("city", "서울특별시");
        dataMap.put("street", "공룡로 50");
        dataMap.put("zipcode", "18888");

        // when
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(memberRepository.findById(member.getMemberId()))
                .expectNextMatches(result ->
                        result.getMemberId() == member.getMemberId() &&
                                result.getName().equals(member.getName()) &&
                                result.getEmail().equals(member.getEmail()) &&
                                result.getPassword().equals(member.getPassword()) &&
                                result.getTel().equals(member.getTel()) &&
                                result.getBirth().equals(member.getBirth()) &&
                                result.getPoint() == member.getPoint() &&
                                result.getRole().equals(member.getRole()) &&
                                result.getCity().equals(member.getCity()) &&
                                result.getStreet().equals(member.getStreet()) &&
                                result.getZipcode().equals(member.getZipcode())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
    }
}