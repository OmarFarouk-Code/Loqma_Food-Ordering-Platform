package com.loqma.customer.service;

import com.loqma.customer.dto.RestaurantDetailsDto;
import com.loqma.customer.repository.CustomerFavoriteRepository;
import com.loqma.customer.repository.CustomerMenuItemRepository;
import com.loqma.customer.repository.CustomerRestaurantRepository;
import com.loqma.owner.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerRestaurantDetailsService {

    private final CustomerRestaurantRepository restaurantRepository;
    private final CustomerMenuItemRepository menuItemRepository;
    private final CustomerFavoriteRepository favoriteRepository;
    private final CurrentCustomerProvider currentCustomer;
    private final DeliveryTimeCalculator deliveryTimeCalculator;

    public RestaurantDetailsDto getRestaurant(String restaurantId, Double lat, Double lng) {
        Restaurant r = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        boolean hasDeals = !menuItemRepository
                .findRestaurantIdsWithDeals(List.of(restaurantId)).isEmpty();
        boolean isFavorite = !favoriteRepository
                .findFavoriteRestaurantIds(currentCustomer.getCurrentCustomerId(), List.of(restaurantId))
                .isEmpty();

        return RestaurantDetailsDto.builder()
                .id(r.getId())
                .name(r.getName())
                .branch(r.getBranch())
                .cuisine(r.getCuisine())
                .logo(r.getLogo())
                .coverImage(r.getCoverImage())
                .address(r.getAddress())
                .phone(r.getPhone())
                .deliveryFee(r.getDeliveryFee())
                .minimumOrderAmount(r.getMinimumOrderAmount())
                .prepTime(r.getPrepTime())
                .maxDeliveryDistance(r.getMaxDeliveryDistance())
                .averageRating(r.getAverageRating())
                .isOpen(r.getIsOpen())
                .gpsLocation(r.getGpsLocation())
                .deliveryTimeMinutes(deliveryTimeCalculator.estimateMinutes(
                        r.getGpsLocation(), r.getPrepTime(), lat, lng))
                .isFavorite(isFavorite)
                .hasDeals(hasDeals)
                .build();
    }
}