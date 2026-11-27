package com.loqma.coupon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 409 - a coupon that has been redeemed can no longer be edited or switched. */
@ResponseStatus(HttpStatus.CONFLICT)
public class CouponLockedException extends RuntimeException {
    public CouponLockedException(String code, String action) {
        super("Coupon " + code + " has already been used and cannot be " + action + ".");
    }
}