package com.loqma.customer.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RestaurantDetailsDto {
    private String id;
    private String name;
    private String branch;
    private String cuisine;
    private String logo;
    private String coverImage;
    private String address;
    private String phone;
    private Double deliveryFee;
    private Double minimumOrderAmount;
    private Double prepTime;
    private Double maxDeliveryDistance;
    private Double averageRating;       
    private Boolean isOpen;
    private String gpsLocation;          // "lat,lng"
    private Integer deliveryTimeMinutes; 
    private Boolean isFavorite;
    private Boolean hasDeals;
}