package com.fml.fluxa.credit.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "credits")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Credit {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditStatus status;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "interest_rate_mv", nullable = false, precision = 8, scale = 4)
    private BigDecimal interestRateMv;

    @Column(name = "current_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "monthly_installment", precision = 15, scale = 2)
    private BigDecimal monthlyInstallment;

    @Column(name = "total_installments")
    private Integer totalInstallments;

    @Column(name = "paid_installments", nullable = false)
    private int paidInstallments;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) status = CreditStatus.ACTIVE;
        if (paidInstallments < 0) paidInstallments = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
