package com.loqma.customer.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CartExtraResponse {
    private String id;
    private String name;
    private Double price;
}
