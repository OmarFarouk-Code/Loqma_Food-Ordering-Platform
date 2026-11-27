package com.loqma.admin.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Admin-created fixed-amount coupon. No usage limit; valid until expiryDate (inclusive). */
@Entity
@Table(name = "coupons",
       uniqueConstraints = @UniqueConstraint(name = "uk_coupons_code", columnNames = "code"))
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Stored upper-case; unique. */
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "minimum_order_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

   

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    
    public boolean isUsed() { return usageCount > 0; }

    public boolean isValid(BigDecimal cartSubtotal) {
        return active && !isExpired() && cartSubtotal.compareTo(minimumOrderAmount) >= 0;
    }

    public void incrementUsage() { usageCount++; }

   
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal v) { this.discountAmount = v; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate d) { this.expiryDate = d; }
    public BigDecimal getMinimumOrderAmount() { return minimumOrderAmount; }
    public void setMinimumOrderAmount(BigDecimal v) { this.minimumOrderAmount = v; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getUsageCount() { return usageCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}