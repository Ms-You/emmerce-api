package commerce.emmerce.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("payment")
public class Payment {

    private String aid;
    @Id
    private String tid;
    private String cid;
    private String partner_order_id;
    private String partner_user_id;
    private String payment_method_type;
    private Integer total_amount;
    private Integer tax_free;
    private Integer vat;
    private Integer point;
    private Integer discount;
    private Integer green_deposit;
    private String purchase_corp;
    private String purchase_corp_code;
    private String issuer_corp;
    private String issuer_corp_code;
    private String bin;
    private String card_type;
    private String install_month;
    private String approved_id;
    private String card_mid;
    private String interest_free_install;
    private String card_item_code;
    private String item_name;
    private Integer quantity;
    private LocalDateTime created_at;
    private LocalDateTime approved_at;


    @Builder
    public Payment(String aid, String tid, String cid, String partner_order_id, String partner_user_id, String payment_method_type,
                   Integer total_amount, Integer tax_free, Integer vat, Integer point, Integer discount, Integer green_deposit,
                   String purchase_corp, String purchase_corp_code, String issuer_corp, String issuer_corp_code, String bin,
                   String card_type, String install_month, String approved_id, String card_mid, String interest_free_install,
                   String card_item_code, String item_name, Integer quantity, LocalDateTime created_at, LocalDateTime approved_at) {
        this.aid = aid;
        this.tid = tid;
        this.cid = cid;
        this.partner_order_id = partner_order_id;
        this.partner_user_id = partner_user_id;
        this.payment_method_type = payment_method_type;
        this.total_amount = total_amount;
        this.tax_free = tax_free;
        this.vat = vat;
        this.point = point;
        this.discount = discount;
        this.green_deposit = green_deposit;
        this.purchase_corp = purchase_corp;
        this.purchase_corp_code = purchase_corp_code;
        this.issuer_corp = issuer_corp;
        this.issuer_corp_code = issuer_corp_code;
        this.bin = bin;
        this.card_type = card_type;
        this.install_month = install_month;
        this.approved_id = approved_id;
        this.card_mid = card_mid;
        this.interest_free_install = interest_free_install;
        this.card_item_code = card_item_code;
        this.item_name = item_name;
        this.quantity = quantity;
        this.created_at = created_at;
        this.approved_at = approved_at;
    }
}
