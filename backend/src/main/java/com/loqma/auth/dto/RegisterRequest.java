package com.loqma.auth.dto;

import com.loqma.common.enums.UserRole;

import java.util.List;

public record RegisterRequest(
        String name, String email, String phone, String password, UserRole role,
        List<AddressRequest> addresses,
        DriverRequest driver,
        RestaurantRequest restaurant) {

    public record AddressRequest(String nickname, String street, String building) {}

    public record DriverRequest(String nationalId, String plateNumber, String drivingLicense) {}

    public record RestaurantRequest(String name, String branch, String cuisine, String address, String phone,
                                    String gpsLocation, Double latitude, Double longitude,
                                    Double deliveryFee, Double minimumOrderAmount,
                                    Double prepTime, Double maxDeliveryDistance) {}
}