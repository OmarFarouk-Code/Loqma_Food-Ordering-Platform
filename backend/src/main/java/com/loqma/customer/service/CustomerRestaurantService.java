package com.loqma.customer.service;

import com.loqma.customer.dto.RestaurantSummaryDto;
import com.loqma.customer.repository.CustomerMenuItemRepository;
import com.loqma.customer.repository.CustomerRestaurantRepository;
import com.loqma.owner.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerRestaurantService {

    private final CustomerRestaurantRepository restaurantRepository;
    private final CustomerMenuItemRepository menuItemRepository;

    public List<RestaurantSummaryDto> listRestaurants(String cuisine, Double minRating,
                                                      String search, boolean dealsOnly) {
        List<Restaurant> restaurants = restaurantRepository.findFiltered(
                blankToNull(cuisine), minRating, blankToNull(search), dealsOnly);

        List<String> ids = restaurants.stream().map(Restaurant::getId).toList();
        Set<String> idsWithDeals = ids.isEmpty()
                ? Set.of()
                : new HashSet<>(menuItemRepository.findRestaurantIdsWithDeals(ids));

        return restaurants.stream()
                .map(r -> toSummary(r, idsWithDeals.contains(r.getId())))
                .toList();
    }

    // An empty text box from the frontend ("") should count as "no filter"
    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private RestaurantSummaryDto toSummary(Restaurant r, boolean hasDeals) {
        return RestaurantSummaryDto.builder()
                .id(r.getId())
                .name(r.getName())
                .branch(r.getBranch())
                .cuisine(r.getCuisine())
                .logo(r.getLogo())
                .coverImage(r.getCoverImage())
                .address(r.getAddress())
                .averageRating(r.getAverageRating())
                .deliveryFee(r.getDeliveryFee())
                .minimumOrderAmount(r.getMinimumOrderAmount())
                .prepTime(r.getPrepTime())
                .isOpen(r.getIsOpen())
                .isFavorite(false)
                .hasDeals(hasDeals)
                .build();
    }
}