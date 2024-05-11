package commerce.emmerce.repository;

import commerce.emmerce.domain.Payment;
import commerce.emmerce.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DataR2dbcTest
class PaymentRepositoryTest {
    private PaymentRepository paymentRepository;
    private DatabaseClient databaseClient;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private FetchSpec<Map<String, Object>> fetchSpec;

    private Payment payment;

    @BeforeEach
    void setup() {
        payment = Payment.builder()
                .aid("요청 고유 번호")
                .tid("결제 고유 번호")
                .cid("가맹점 코드")
                .partner_order_id("1")
                .partner_user_id("1")
                .payment_method_type("CARD")
                .total_amount(40000)
                .tax_free(0)
                .vat(4000)
                .point(0)
                .discount(10000)
                .green_deposit(0)
                .purchase_corp("매입 카드사 한글명")
                .purchase_corp_code("매입 카드사 코드")
                .issuer_corp("카드 발급사 한글명")
                .issuer_corp_code("카드 발급사 코드")
                .bin("카드 BIN")
                .card_type("카드 타입")
                .install_month("12")
                .approved_id("카드사 승인번호")
                .card_mid("카드사 가맹점 번호")
                .interest_free_install("Y")
                .card_item_code("카드 상품 코드")
                .item_name("샴푸")
                .quantity(5)
                .created_at(LocalDateTime.now())
                .approved_at(LocalDateTime.now())
                .paymentStatus(PaymentStatus.COMPLETE)
                .build();

        databaseClient = mock(DatabaseClient.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        fetchSpec = mock(FetchSpec.class);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.fetch()).thenReturn(fetchSpec);

        paymentRepository = new PaymentRepository(databaseClient);
    }

    @Test
    void saveTemporary() {
        // given
        // when
        when(executeSpec.bind("tid", payment.getTid())).thenReturn(executeSpec);
        when(executeSpec.bind("cid", payment.getCid())).thenReturn(executeSpec);
        when(executeSpec.bind("partner_order_id", payment.getPartner_order_id())).thenReturn(executeSpec);
        when(executeSpec.bind("partner_user_id", payment.getPartner_user_id())).thenReturn(executeSpec);
        when(executeSpec.bind("paymentStatus", payment.getPaymentStatus().name())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(paymentRepository.saveTemporary(payment))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(5)).bind(anyString(), any());
    }

    @Test
    void save() {
        // given
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("aid", payment.getAid());
        dataMap.put("tid", "결제 고유 번호");
        dataMap.put("cid", payment.getCid());
        dataMap.put("partner_order_id", payment.getPartner_order_id());
        dataMap.put("partner_user_id", payment.getPartner_user_id());
        dataMap.put("payment_method_type", payment.getPayment_method_type());
        dataMap.put("total_amount", payment.getTotal_amount());
        dataMap.put("tax_free", payment.getTax_free());
        dataMap.put("vat", payment.getVat());
        dataMap.put("point", payment.getPoint());
        dataMap.put("discount", payment.getDiscount());
        dataMap.put("green_deposit", payment.getGreen_deposit());
        dataMap.put("purchase_corp", payment.getPurchase_corp());
        dataMap.put("purchase_corp_code", payment.getPurchase_corp_code());
        dataMap.put("issuer_corp", payment.getIssuer_corp());
        dataMap.put("issuer_corp_code", payment.getIssuer_corp_code());
        dataMap.put("bin", payment.getBin());
        dataMap.put("card_type", payment.getCard_type());
        dataMap.put("install_month", payment.getInstall_month());
        dataMap.put("approved_id", payment.getApproved_id());
        dataMap.put("card_mid", payment.getCard_mid());
        dataMap.put("interest_free_install", payment.getInterest_free_install());
        dataMap.put("card_item_code", payment.getCard_item_code());
        dataMap.put("item_name", payment.getItem_name());
        dataMap.put("quantity", payment.getQuantity());
        dataMap.put("created_at", payment.getCreated_at());
        dataMap.put("approved_at", payment.getApproved_at());
        dataMap.put("payment_status", payment.getPaymentStatus().name());

        // when
        when(executeSpec.bind("aid", payment.getAid())).thenReturn(executeSpec);
        when(executeSpec.bind("tid", payment.getTid())).thenReturn(executeSpec);
        when(executeSpec.bind("cid", payment.getCid())).thenReturn(executeSpec);
        when(executeSpec.bind("partner_order_id", payment.getPartner_order_id())).thenReturn(executeSpec);
        when(executeSpec.bind("partner_user_id", payment.getPartner_user_id())).thenReturn(executeSpec);
        when(executeSpec.bind("payment_method_type", payment.getPayment_method_type())).thenReturn(executeSpec);
        when(executeSpec.bind("total_amount", payment.getTotal_amount())).thenReturn(executeSpec);
        when(executeSpec.bind("tax_free", payment.getTax_free())).thenReturn(executeSpec);
        when(executeSpec.bind("vat", payment.getVat())).thenReturn(executeSpec);
        when(executeSpec.bind("point", payment.getPoint())).thenReturn(executeSpec);
        when(executeSpec.bind("discount", payment.getDiscount())).thenReturn(executeSpec);
        when(executeSpec.bind("green_deposit", payment.getGreen_deposit())).thenReturn(executeSpec);
        when(executeSpec.bind("purchase_corp", payment.getPurchase_corp())).thenReturn(executeSpec);
        when(executeSpec.bind("purchase_corp_code", payment.getPurchase_corp_code())).thenReturn(executeSpec);
        when(executeSpec.bind("issuer_corp", payment.getIssuer_corp())).thenReturn(executeSpec);
        when(executeSpec.bind("issuer_corp_code", payment.getIssuer_corp_code())).thenReturn(executeSpec);
        when(executeSpec.bind("bin", payment.getBin())).thenReturn(executeSpec);
        when(executeSpec.bind("card_type", payment.getCard_type())).thenReturn(executeSpec);
        when(executeSpec.bind("install_month", payment.getInstall_month())).thenReturn(executeSpec);
        when(executeSpec.bind("approved_id", payment.getApproved_id())).thenReturn(executeSpec);
        when(executeSpec.bind("card_mid", payment.getCard_mid())).thenReturn(executeSpec);
        when(executeSpec.bind("interest_free_install", payment.getInterest_free_install())).thenReturn(executeSpec);
        when(executeSpec.bind("card_item_code", payment.getCard_item_code())).thenReturn(executeSpec);
        when(executeSpec.bind("item_name", payment.getItem_name())).thenReturn(executeSpec);
        when(executeSpec.bind("quantity", payment.getQuantity())).thenReturn(executeSpec);
        when(executeSpec.bind("created_at", payment.getCreated_at())).thenReturn(executeSpec);
        when(executeSpec.bind("approved_at", payment.getApproved_at())).thenReturn(executeSpec);
        when(executeSpec.bind("paymentStatus", payment.getPaymentStatus().name())).thenReturn(executeSpec);

        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(paymentRepository.save(payment))
                .expectNextMatches(result ->
                        result.getAid().equals(payment.getAid()) &&
                                result.getTid().equals(payment.getTid()) &&
                                result.getCid().equals(payment.getCid()) &&
                                result.getPartner_order_id().equals(payment.getPartner_order_id()) &&
                                result.getPartner_user_id().equals(payment.getPartner_user_id()) &&
                                result.getPayment_method_type().equals(payment.getPayment_method_type()) &&
                                result.getTotal_amount() == payment.getTotal_amount() &&
                                result.getTax_free() == payment.getTax_free() &&
                                result.getVat() == payment.getVat() &&
                                result.getPoint() == payment.getPoint() &&
                                result.getDiscount() == payment.getDiscount() &&
                                result.getGreen_deposit() == payment.getGreen_deposit() &&
                                result.getPurchase_corp().equals(payment.getPurchase_corp()) &&
                                result.getPurchase_corp_code().equals(payment.getPurchase_corp_code()) &&
                                result.getIssuer_corp().equals(payment.getIssuer_corp()) &&
                                result.getIssuer_corp_code().equals(payment.getIssuer_corp_code()) &&
                                result.getBin().equals(payment.getBin()) &&
                                result.getCard_type().equals(payment.getCard_type()) &&
                                result.getInstall_month().equals(payment.getInstall_month()) &&
                                result.getApproved_id().equals(payment.getApproved_id()) &&
                                result.getCard_mid().equals(payment.getCard_mid()) &&
                                result.getInterest_free_install().equals(payment.getInterest_free_install()) &&
                                result.getCard_item_code().equals(payment.getCard_item_code()) &&
                                result.getItem_name().equals(payment.getItem_name()) &&
                                result.getQuantity() == payment.getQuantity() &&
                                result.getCreated_at().isEqual(payment.getCreated_at()) &&
                                result.getApproved_at().isEqual(payment.getApproved_at()) &&
                                result.getPaymentStatus().equals(payment.getPaymentStatus())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(28)).bind(anyString(), any());
    }

    @Test
    void findByOrderId() {
        // given
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("aid", payment.getAid());
        dataMap.put("tid", "결제 고유 번호");
        dataMap.put("cid", payment.getCid());
        dataMap.put("partner_order_id", payment.getPartner_order_id());
        dataMap.put("partner_user_id", payment.getPartner_user_id());
        dataMap.put("payment_method_type", payment.getPayment_method_type());
        dataMap.put("total_amount", payment.getTotal_amount());
        dataMap.put("tax_free", payment.getTax_free());
        dataMap.put("vat", payment.getVat());
        dataMap.put("point", payment.getPoint());
        dataMap.put("discount", payment.getDiscount());
        dataMap.put("green_deposit", payment.getGreen_deposit());
        dataMap.put("purchase_corp", payment.getPurchase_corp());
        dataMap.put("purchase_corp_code", payment.getPurchase_corp_code());
        dataMap.put("issuer_corp", payment.getIssuer_corp());
        dataMap.put("issuer_corp_code", payment.getIssuer_corp_code());
        dataMap.put("bin", payment.getBin());
        dataMap.put("card_type", payment.getCard_type());
        dataMap.put("install_month", payment.getInstall_month());
        dataMap.put("approved_id", payment.getApproved_id());
        dataMap.put("card_mid", payment.getCard_mid());
        dataMap.put("interest_free_install", payment.getInterest_free_install());
        dataMap.put("card_item_code", payment.getCard_item_code());
        dataMap.put("item_name", payment.getItem_name());
        dataMap.put("quantity", payment.getQuantity());
        dataMap.put("created_at", payment.getCreated_at());
        dataMap.put("approved_at", payment.getApproved_at());
        dataMap.put("payment_status", payment.getPaymentStatus().name());

        // when
        when(executeSpec.bind("orderId", payment.getPartner_order_id())).thenReturn(executeSpec);
        when(fetchSpec.one()).thenReturn(Mono.just(dataMap));

        StepVerifier.create(paymentRepository.findByOrderId(Long.parseLong(payment.getPartner_order_id())))
                .expectNextMatches(result ->
                        result.getAid().equals(payment.getAid()) &&
                                result.getTid().equals(payment.getTid()) &&
                                result.getCid().equals(payment.getCid()) &&
                                result.getPartner_order_id().equals(payment.getPartner_order_id()) &&
                                result.getPartner_user_id().equals(payment.getPartner_user_id()) &&
                                result.getPayment_method_type().equals(payment.getPayment_method_type()) &&
                                result.getTotal_amount() == payment.getTotal_amount() &&
                                result.getTax_free() == payment.getTax_free() &&
                                result.getVat() == payment.getVat() &&
                                result.getPoint() == payment.getPoint() &&
                                result.getDiscount() == payment.getDiscount() &&
                                result.getGreen_deposit() == payment.getGreen_deposit() &&
                                result.getPurchase_corp().equals(payment.getPurchase_corp()) &&
                                result.getPurchase_corp_code().equals(payment.getPurchase_corp_code()) &&
                                result.getIssuer_corp().equals(payment.getIssuer_corp()) &&
                                result.getIssuer_corp_code().equals(payment.getIssuer_corp_code()) &&
                                result.getBin().equals(payment.getBin()) &&
                                result.getCard_type().equals(payment.getCard_type()) &&
                                result.getInstall_month().equals(payment.getInstall_month()) &&
                                result.getApproved_id().equals(payment.getApproved_id()) &&
                                result.getCard_mid().equals(payment.getCard_mid()) &&
                                result.getInterest_free_install().equals(payment.getInterest_free_install()) &&
                                result.getCard_item_code().equals(payment.getCard_item_code()) &&
                                result.getItem_name().equals(payment.getItem_name()) &&
                                result.getQuantity() == payment.getQuantity() &&
                                result.getCreated_at().isEqual(payment.getCreated_at()) &&
                                result.getApproved_at().isEqual(payment.getApproved_at()) &&
                                result.getPaymentStatus().equals(payment.getPaymentStatus())
                ).verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(1)).bind(anyString(), any());
    }

    @Test
    void updateStatus() {
        // given
        PaymentStatus paymentStatus = PaymentStatus.CANCEL;

        // when
        when(executeSpec.bind("paymentStatus", paymentStatus.name())).thenReturn(executeSpec);
        when(executeSpec.bind("tid", payment.getTid())).thenReturn(executeSpec);
        when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

        StepVerifier.create(paymentRepository.updateStatus(payment.getTid(), paymentStatus))
                .verifyComplete();

        // then
        verify(databaseClient, times(1)).sql(anyString());
        verify(executeSpec, times(2)).bind(anyString(), any());
    }
}