package com.loqma.customer.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CartItemResponse {
    private String id;
    private String menuItemId;
    private String menuItemName;
    private String selectedSizeId;
    private String selectedSizeName;
    private List<CartExtraResponse> selectedExtras;
    private Integer quantity;
    private String customerNote;
    private Double linePrice;
}
