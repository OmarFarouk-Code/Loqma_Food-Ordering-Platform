package com.loqma.customer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loqma.customer.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findByCartId(String cartId);
}
