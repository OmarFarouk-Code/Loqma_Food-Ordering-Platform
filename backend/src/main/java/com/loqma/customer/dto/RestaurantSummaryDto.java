package com.loqma.customer.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RestaurantSummaryDto {
    private Integer deliveryTimeMinutes;
    private String id;
    private String name;
    private String branch;
    private String cuisine;
    private String logo;
    private String coverImage;
    private String address;         
    private Double averageRating;
    private Double deliveryFee;
    private Double minimumOrderAmount;
    private Double prepTime;
    private Boolean isOpen;
    private Boolean isFavorite;     
    private Boolean hasDeals;
}