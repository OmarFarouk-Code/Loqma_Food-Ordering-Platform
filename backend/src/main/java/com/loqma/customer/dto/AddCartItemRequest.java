package com.loqma.customer.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCartItemRequest {
    private String menuItemId;
    private String selectedSizeId;
    private List<String> selectedExtraIds;
    private Integer quantity;
    private String customerNote;
}
