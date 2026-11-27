package com.loqma.customer.controller;

import com.loqma.customer.dto.PagedResponse;
import com.loqma.customer.dto.RestaurantSummaryDto;
import com.loqma.customer.service.CustomerRestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/restaurants")
@RequiredArgsConstructor
public class CustomerRestaurantController {

    private final CustomerRestaurantService service;

    @GetMapping
    public PagedResponse<RestaurantSummaryDto> list(
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean deals,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Integer maxDeliveryTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.listRestaurants(cuisine, minRating, search, deals,
                lat, lng, maxDeliveryTime, page, size);
    }

    @PostMapping("/{id}/favorite")
    public Map<String, Boolean> toggleFavorite(@PathVariable String id) {
        return Map.of("isFavorite", service.toggleFavorite(id));
    }
}