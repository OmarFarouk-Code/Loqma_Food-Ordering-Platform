package com.loqma.coupon;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Body for create and update. */
public record CouponRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 32) String code,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal discountAmount,
        @NotNull LocalDate expiryDate,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal minimumOrderAmount
) { }