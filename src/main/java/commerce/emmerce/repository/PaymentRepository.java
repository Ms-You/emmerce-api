package commerce.emmerce.repository;

import commerce.emmerce.domain.Payment;
import commerce.emmerce.domain.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Repository
public class PaymentRepository {

    private final DatabaseClient databaseClient;

    public Mono<Void> saveTemporary(Payment payment) {
        String query = """
                insert into payment (tid, cid, partner_order_id, partner_user_id, payment_status)
                values (:tid, :cid, :partner_order_id, :partner_user_id, :paymentStatus)
                """;

        return databaseClient.sql(query)
                .bind("tid", payment.getTid())
                .bind("cid", payment.getCid())
                .bind("partner_order_id", payment.getPartner_order_id())
                .bind("partner_user_id", payment.getPartner_user_id())
                .bind("paymentStatus", payment.getPaymentStatus().name())
                .then();
    }

    public Mono<Payment> save(Payment payment) {
        String query = """
                insert into payment (aid, tid, cid, partner_order_id, partner_user_id, payment_method_type, total_amount,
                                    tax_free, vat, point, discount, green_deposit, purchase_corp, purchase_corp_code,
                                    issuer_corp, issuer_corp_code, bin, card_type, install_month, approved_id, card_mid,
                                    interest_free_install, card_item_code, item_name, quantity, created_at, approved_at, payment_status)
                values (:aid, :tid, :cid, :partner_order_id, :partner_user_id, :payment_method_type, :total_amount,
                        :tax_free, :vat, :point, :discount, :green_deposit, :purchase_corp, :purchase_corp_code,
                        :issuer_corp, :issuer_corp_code, :bin, :card_type, :install_month, :approved_id, :card_mid,
                        :interest_free_install, :card_item_code, :item_name, :quantity, :created_at, :approved_at, :paymentStatus)
                on conflict (tid) do update
                set aid = :aid, cid = :cid, partner_order_id = :partner_order_id, partner_user_id = :partner_user_id,
                                    payment_method_type = :payment_method_type, total_amount = :total_amount, tax_free = :tax_free, vat = :vat,
                                    point = :point, discount = :discount, green_deposit = :green_deposit, purchase_corp = :purchase_corp,
                                    purchase_corp_code = :purchase_corp_code, issuer_corp = :issuer_corp, issuer_corp_code = :issuer_corp_code,
                                    bin = :bin, card_type = :card_type, install_month = :install_month, approved_id = :approved_id,
                                    card_mid = :card_mid, interest_free_install = :interest_free_install, card_item_code = :card_item_code,
                                    item_name = :item_name, quantity = :quantity, created_at = :created_at, approved_at = :approved_at, payment_status = :paymentStatus
                returning *
                """;

        return databaseClient.sql(query)
                .bind("aid", payment.getAid())
                .bind("tid", payment.getTid())
                .bind("cid", payment.getCid())
                .bind("partner_order_id", payment.getPartner_order_id())
                .bind("partner_user_id", payment.getPartner_user_id())
                .bind("payment_method_type", payment.getPayment_method_type())
                .bind("total_amount", payment.getTotal_amount())
                .bind("tax_free", payment.getTax_free())
                .bind("vat", payment.getVat())
                .bind("point", payment.getPoint())
                .bind("discount", payment.getDiscount())
                .bind("green_deposit", payment.getGreen_deposit())
                .bind("purchase_corp", payment.getPurchase_corp())
                .bind("purchase_corp_code", payment.getPurchase_corp_code())
                .bind("issuer_corp", payment.getIssuer_corp())
                .bind("issuer_corp_code", payment.getIssuer_corp_code())
                .bind("bin", payment.getBin())
                .bind("card_type", payment.getCard_type())
                .bind("install_month", payment.getInstall_month())
                .bind("approved_id", payment.getApproved_id())
                .bind("card_mid", payment.getCard_mid())
                .bind("interest_free_install", payment.getInterest_free_install())
                .bind("card_item_code", payment.getCard_item_code())
                .bind("item_name", payment.getItem_name())
                .bind("quantity", payment.getQuantity())
                .bind("created_at", payment.getCreated_at())
                .bind("approved_at", payment.getApproved_at())
                .bind("paymentStatus", payment.getPaymentStatus().name())
                .fetch().one()
                .map(row -> Payment.builder()
                        .aid((String)row.get("aid"))
                        .tid((String)row.get("tid"))
                        .cid((String)row.get("cid"))
                        .partner_order_id((String)row.get("partner_order_id"))
                        .partner_user_id((String)row.get("partner_user_id"))
                        .payment_method_type((String)row.get("payment_method_type"))
                        .total_amount((Integer)row.get("total_amount"))
                        .tax_free((Integer)row.get("tax_free"))
                        .vat((Integer)row.get("vat"))
                        .point((Integer)row.get("point"))
                        .discount((Integer)row.get("discount"))
                        .green_deposit((Integer)row.get("green_deposit"))
                        .purchase_corp((String)row.get("purchase_corp"))
                        .purchase_corp_code((String)row.get("purchase_corp_code"))
                        .issuer_corp((String)row.get("issuer_corp"))
                        .issuer_corp_code((String)row.get("issuer_corp_code"))
                        .bin((String)row.get("bin"))
                        .card_type((String)row.get("cart_type"))
                        .install_month((String)row.get("install_month"))
                        .approved_id((String)row.get("approved_id"))
                        .card_mid((String)row.get("card_mid"))
                        .interest_free_install((String)row.get("interest_free_install"))
                        .card_item_code((String)row.get("card_item_code"))
                        .item_name((String)row.get("item_name"))
                        .quantity((Integer)row.get("quantity"))
                        .created_at((LocalDateTime) row.get("created_at"))
                        .approved_at((LocalDateTime) row.get("approved_at"))
                        .paymentStatus(PaymentStatus.valueOf((String) row.get("payment_status")))
                        .build());
    }

    public Mono<Payment> findByOrderId(Long orderId) {
        String query = """
                select *
                from payment p
                where p.partner_order_id = :orderId
                """;

        return databaseClient.sql(query)
                .bind("orderId", String.valueOf(orderId))
                .fetch().one()
                .map(row -> Payment.builder()
                        .aid((String)row.get("aid"))
                        .tid((String)row.get("tid"))
                        .cid((String)row.get("cid"))
                        .partner_order_id((String)row.get("partner_order_id"))
                        .partner_user_id((String)row.get("partner_user_id"))
                        .payment_method_type((String)row.get("payment_method_type"))
                        .total_amount((Integer)row.get("total_amount"))
                        .tax_free((Integer)row.get("tax_free"))
                        .vat((Integer)row.get("vat"))
                        .point((Integer)row.get("point"))
                        .discount((Integer)row.get("discount"))
                        .green_deposit((Integer)row.get("green_deposit"))
                        .purchase_corp((String)row.get("purchase_corp"))
                        .purchase_corp_code((String)row.get("purchase_corp_code"))
                        .issuer_corp((String)row.get("issuer_corp"))
                        .issuer_corp_code((String)row.get("issuer_corp_code"))
                        .bin((String)row.get("bin"))
                        .card_type((String)row.get("cart_type"))
                        .install_month((String)row.get("install_month"))
                        .approved_id((String)row.get("approved_id"))
                        .card_mid((String)row.get("card_mid"))
                        .interest_free_install((String)row.get("interest_free_install"))
                        .card_item_code((String)row.get("card_item_code"))
                        .item_name((String)row.get("item_name"))
                        .quantity((Integer)row.get("quantity"))
                        .created_at((LocalDateTime) row.get("created_at"))
                        .approved_at((LocalDateTime) row.get("approved_at"))
                        .paymentStatus(PaymentStatus.valueOf((String) row.get("payment_status")))
                        .build());
    }

    public Mono<Void> updateStatus(String tid, PaymentStatus paymentStatus) {
        String query = """
                update payment
                set payment_status = :paymentStatus
                where tid = :tid
                """;

        return databaseClient.sql(query)
                .bind("paymentStatus", paymentStatus.name())
                .bind("tid", tid)
                .then();
    }

}
