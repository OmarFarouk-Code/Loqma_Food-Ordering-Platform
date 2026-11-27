package com.loqma.coupon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 404 - no coupon with that id. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CouponNotFoundException extends RuntimeException {
    public CouponNotFoundException(String id) {
        super("Coupon not found: " + id);
    }
}