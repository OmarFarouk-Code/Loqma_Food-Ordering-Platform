package com.loqma.customer.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class RestaurantSummaryDto {
    private String id;
    private String name;
    private String branch;
    private String cuisine;
    private String logo;
    private Double averageRating;
    private Double deliveryFee;
    private Double minimumOrderAmount;
    private Double prepTime;
    private Boolean isOpen;
}
