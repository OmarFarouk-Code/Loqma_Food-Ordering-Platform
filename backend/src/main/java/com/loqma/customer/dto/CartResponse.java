package com.loqma.customer.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder 
@Getter
public class CartResponse {
    private List<CartItemResponse> cartItems;
}
