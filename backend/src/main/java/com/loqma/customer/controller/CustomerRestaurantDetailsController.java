package com.loqma.customer.controller;

import com.loqma.customer.dto.RestaurantDetailsDto;
import com.loqma.customer.service.CustomerRestaurantDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/restaurants")
@RequiredArgsConstructor
public class CustomerRestaurantDetailsController {

    private final CustomerRestaurantDetailsService service;

    @GetMapping("/{restaurantId}")
    public RestaurantDetailsDto getById(
            @PathVariable String restaurantId,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {
        return service.getRestaurant(restaurantId, lat, lng);
    }
}