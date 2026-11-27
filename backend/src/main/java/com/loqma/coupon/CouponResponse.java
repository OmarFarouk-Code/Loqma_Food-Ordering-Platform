package com.loqma.coupon;

import com.loqma.order.entity.Coupon;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** What the API returns for one coupon. */
public record CouponResponse(
        String id,
        String name,
        String code,
        BigDecimal discountAmount,
        LocalDate expiryDate,
        BigDecimal minimumOrderAmount,
        boolean active,
        int usageCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static CouponResponse from(Coupon c) {
        return new CouponResponse(
                c.getId(),
                c.getName(),
                c.getCode(),
                c.getDiscountAmount() == null ? BigDecimal.ZERO : BigDecimal.valueOf(c.getDiscountAmount()),
                c.getExpiryDate(),
                c.getMinimumOrderAmount() == null ? BigDecimal.ZERO : BigDecimal.valueOf(c.getMinimumOrderAmount()),
                Boolean.TRUE.equals(c.getIsActive()),
                c.getUsageCount() == null ? 0 : c.getUsageCount(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
