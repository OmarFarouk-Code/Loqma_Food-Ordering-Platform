package com.loqma.customer.service;

import com.loqma.customer.dto.RestaurantSummaryDto;
import com.loqma.customer.repository.CustomerRestaurantRepository;
import com.loqma.owner.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerRestaurantService {

    private final CustomerRestaurantRepository restaurantRepository;

    public List<RestaurantSummaryDto> listRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    private RestaurantSummaryDto toSummary(Restaurant r) {
        return RestaurantSummaryDto.builder()
                .id(r.getId())
                .name(r.getName())
                .branch(r.getBranch())
                .cuisine(r.getCuisine())
                .logo(r.getLogo())
                .averageRating(r.getAverageRating())
                .deliveryFee(r.getDeliveryFee())
                .minimumOrderAmount(r.getMinimumOrderAmount())
                .prepTime(r.getPrepTime())
                .isOpen(r.getIsOpen())
                .build();
    }
}