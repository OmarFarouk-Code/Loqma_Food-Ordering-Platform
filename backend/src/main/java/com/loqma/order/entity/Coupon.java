package com.loqma.order.entity;

import com.loqma.admin.entity.Admin;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupons_active_expiry", columnList = "is_active, expiry_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_admin_id")
    private Admin createdByAdmin;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "discount_amount", nullable = false)
    private Double discountAmount;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "minimum_order_amount", nullable = false)
    @Builder.Default
    private Double minimumOrderAmount = 0.0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isExpired() {
        return expiryDate != null && LocalDate.now().isAfter(expiryDate);
    }

    public boolean isUsed() {
        return usageCount != null && usageCount > 0;
    }

    public boolean isValid(double cartSubtotal) {
        double min = minimumOrderAmount == null ? 0.0 : minimumOrderAmount;
        return Boolean.TRUE.equals(isActive) && !isExpired() && cartSubtotal >= min;
    }

    public void incrementUsage() {
        usageCount = (usageCount == null ? 0 : usageCount) + 1;
    }
}
