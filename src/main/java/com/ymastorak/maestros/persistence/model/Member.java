package com.ymastorak.maestros.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "members")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberType type;

    @Column(nullable = false)
    private BigDecimal legacyOutstanding;

    @Column(nullable = false)
    private BigDecimal currentOutstanding;

    @Column(nullable = false)
    private BigDecimal totalOutstanding;

    @Column(nullable = false)
    private ZonedDateTime registrationDate;

    @Column(nullable = false)
    private ZonedDateTime activationDate;

    @Column(nullable = false)
    private ZonedDateTime lastUpdateDate;

    @Column(nullable = false)
    private ZonedDateTime lastPresenceDate;

    @Column(nullable = false)
    private int justifiedAbsences;

    @Column(nullable = false)
    private int absences;

    @Column(unique = true)
    private String accessCardId;

    @Column(unique = true)
    private String reportsName;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> extra;

    @JsonIgnore
    public String getFullName() {
        return name + " " + surname;
    }

    public Member activate(ZonedDateTime eventDate) {
        return this
                .setStatus(MemberStatus.ACTIVE)
                .setActivationDate(eventDate);
    }

    public Member updateLastPresenceDate(ZonedDateTime eventDate) {
        return this
                .setJustifiedAbsences(0)
                .setAbsences(0)
                .setLastPresenceDate(eventDate);
    }

    public Member moveCurrentOutstandingToLegacy() {
        BigDecimal legacyOutstanding = this.getLegacyOutstanding().add(this.getCurrentOutstanding());
        return this
                .setLegacyOutstanding(legacyOutstanding)
                .setCurrentOutstanding(BigDecimal.ZERO);
    }

    public Member applyCharge(BigDecimal amount) {
        return this
                .setCurrentOutstanding(this.getCurrentOutstanding().add(amount))
                .setTotalOutstanding(this.getTotalOutstanding().add(amount));
    }

    public Member applyPayment(BigDecimal paymentAmount) {
        BigDecimal paymentAmountLeft = BigDecimal.valueOf(paymentAmount.doubleValue());

        if (paymentAmountLeft.compareTo(this.getLegacyOutstanding()) >= 0) { // payment amount left >= legacy outstanding
            paymentAmountLeft = paymentAmountLeft.subtract(this.getLegacyOutstanding());
            this.setTotalOutstanding(this.getTotalOutstanding().subtract(this.getLegacyOutstanding()));
            this.setLegacyOutstanding(BigDecimal.ZERO);
        } else {
            this.setTotalOutstanding(this.getTotalOutstanding().subtract(paymentAmountLeft));
            this.setLegacyOutstanding(this.getLegacyOutstanding().subtract(paymentAmountLeft));
            paymentAmountLeft = BigDecimal.ZERO;
        }

        if (paymentAmountLeft.compareTo(this.getCurrentOutstanding()) >= 0) { // payment amount left >= current outstanding
            this.setTotalOutstanding(this.getTotalOutstanding().subtract(this.getCurrentOutstanding()));
            this.setCurrentOutstanding(BigDecimal.ZERO);
        } else {
            this.setTotalOutstanding(this.getTotalOutstanding().subtract(paymentAmountLeft));
            this.setCurrentOutstanding(this.getCurrentOutstanding().subtract(paymentAmountLeft));
        }

        return this;
    }

    public Member updateExtra(String key, String value) {
        Map<String, Object> memberExtra = this.getExtra() != null ? this.getExtra() : new HashMap<>();
        memberExtra.put(key, value);
        return this.setExtra(memberExtra);
    }
}
