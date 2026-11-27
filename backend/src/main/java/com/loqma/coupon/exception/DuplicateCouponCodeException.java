package com.loqma.coupon.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 409 - that code is already taken. */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateCouponCodeException extends RuntimeException {
    public DuplicateCouponCodeException(String code) {
        super("Coupon code already exists: " + code);
    }
}