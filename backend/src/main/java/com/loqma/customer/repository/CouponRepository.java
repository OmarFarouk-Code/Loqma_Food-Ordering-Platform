package com.loqma.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loqma.order.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, String>{
    Coupon findByCode(String code);
}
