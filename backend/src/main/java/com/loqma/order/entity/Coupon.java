package com.loqma.order.entity;

import com.loqma.admin.entity.Admin;
import jakarta.persistence.*;
import lombok.*;

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
}