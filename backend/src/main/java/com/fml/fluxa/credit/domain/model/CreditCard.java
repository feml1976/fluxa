package com.fml.fluxa.credit.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "credit_cards")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CreditCard {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credit_id", nullable = false, unique = true)
    private Long creditId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "card_number_last4", nullable = false, length = 4)
    private String cardNumberLast4;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardBrand brand;

    @Column(name = "credit_limit_purchases", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditLimitPurchases;

    @Column(name = "credit_limit_advances", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditLimitAdvances;

    @Column(name = "available_purchases", nullable = false, precision = 15, scale = 2)
    private BigDecimal availablePurchases;

    @Column(name = "available_advances", nullable = false, precision = 15, scale = 2)
    private BigDecimal availableAdvances;

    @Column(name = "previous_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal previousBalance;

    @Column(name = "minimum_payment", nullable = false, precision = 15, scale = 2)
    private BigDecimal minimumPayment;

    @Column(name = "alternate_minimum_payment", nullable = false, precision = 15, scale = 2)
    private BigDecimal alternateMinimumPayment;

    @Column(name = "late_interest", nullable = false, precision = 15, scale = 2)
    private BigDecimal lateInterest;

    @Column(name = "payment_due_day", nullable = false)
    private int paymentDueDay;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
