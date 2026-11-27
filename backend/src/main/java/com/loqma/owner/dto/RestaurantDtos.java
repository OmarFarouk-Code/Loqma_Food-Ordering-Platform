package com.loqma.owner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public final class RestaurantDtos {

    private RestaurantDtos() {}

    /** averageRating and open are NOT here on purpose: the owner can't edit them in this form. */
    public record ProfileRequest(
            @Size(max = 100) String ownerName,
            @Size(max = 20) String ownerPhone,
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 100) String branch,
            @NotBlank @Size(max = 100) String cuisine,
            @Size(max = 500) String logo,
            @Size(max = 500) String coverImage,
            @Size(max = 255) String address,
            @Size(max = 20) String phone,
            @NotNull @PositiveOrZero Double deliveryFee,
            @NotNull @PositiveOrZero Double minimumOrderAmount,
            @NotNull @PositiveOrZero Double prepTime,
            @NotNull @PositiveOrZero Double maxDeliveryDistance,
            @Size(max = 100) String gpsLocation) {}

    public record ProfileResponse(
            String id,
            String ownerName,
            String ownerPhone,
            String name,
            String branch,
            String cuisine,
            String logo,
            String coverImage,
            String address,
            String phone,
            Double deliveryFee,
            Double minimumOrderAmount,
            Double prepTime,
            Double maxDeliveryDistance,
            Double averageRating,
            boolean open,
            String gpsLocation) {}
}