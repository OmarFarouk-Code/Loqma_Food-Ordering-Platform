package com.loqma.customer.dto;

import java.util.List;

import com.loqma.customer.entity.CartItem;

import lombok.Builder;

@Builder 
public class CartResponse {
    private List<CartItem> cartItems;
}
