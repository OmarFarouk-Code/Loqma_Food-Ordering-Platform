package com.loqma.owner.controller;

import com.loqma.owner.dto.RestaurantDtos.ProfileRequest;
import com.loqma.owner.dto.RestaurantDtos.ProfileResponse;
import com.loqma.owner.service.OwnerRestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/restaurant")
@RequiredArgsConstructor
public class OwnerRestaurantController {

    private final OwnerRestaurantService restaurantService;

    @GetMapping
    public ProfileResponse get() {
        return restaurantService.get();
    }

    @PutMapping
    public ProfileResponse update(@Valid @RequestBody ProfileRequest request) {
        return restaurantService.update(request);
    }
}