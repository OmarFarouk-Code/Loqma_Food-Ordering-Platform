package com.loqma.customer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loqma.customer.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, String> {
    List<Cart> findByCartId(String cartId);
}
