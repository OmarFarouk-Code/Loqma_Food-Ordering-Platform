package com.loqma.customer.controller;

import com.loqma.customer.dto.RestaurantSummaryDto;
import com.loqma.customer.service.CustomerRestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customer/restaurants")
@RequiredArgsConstructor
public class CustomerRestaurantController {

    private final CustomerRestaurantService service;

    @GetMapping
    public List<RestaurantSummaryDto> list() {
        return service.listRestaurants();
    }
}