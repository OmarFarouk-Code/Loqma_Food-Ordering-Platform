package com.loqma.customer.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CartReceiptResponse {
    private String id;
    private List<CartItemResponse> items;
    private Double subtotal;
    private Double deliveryFee;
    private Double serviceFee;
    private Double tax;
    private Double discount;
    private Double totalPrice;
    private Double minimumOrderAmount;
}
