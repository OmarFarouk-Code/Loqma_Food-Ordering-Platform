package com.loqma.customer.controller;

import com.loqma.customer.dto.RestaurantReviewsResponse;
import com.loqma.customer.service.CustomerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/restaurants/{restaurantId}/reviews")
@RequiredArgsConstructor
public class CustomerReviewController {

    private final CustomerReviewService service;

    @GetMapping
    public RestaurantReviewsResponse list(
            @PathVariable String restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.getReviews(restaurantId, page, size);
    }
}