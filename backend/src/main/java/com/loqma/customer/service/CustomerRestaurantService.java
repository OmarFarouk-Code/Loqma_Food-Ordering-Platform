package com.loqma.customer.service;

import com.loqma.customer.dto.PagedResponse;
import com.loqma.customer.dto.RestaurantSummaryDto;
import com.loqma.customer.repository.CustomerFavoriteRepository;
import com.loqma.customer.repository.CustomerMenuItemRepository;
import com.loqma.customer.repository.CustomerRestaurantRepository;
import com.loqma.owner.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerRestaurantService {

    private static final int MAX_PAGE_SIZE = 50;

    private final CustomerRestaurantRepository restaurantRepository;
    private final CustomerMenuItemRepository menuItemRepository;
    private final CustomerFavoriteRepository favoriteRepository;
    private final CurrentCustomerProvider currentCustomer;

    public PagedResponse<RestaurantSummaryDto> listRestaurants(String cuisine, Double minRating,
                                                               String search, boolean dealsOnly,
                                                               int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.DESC, "averageRating")
                    .and(Sort.by(Sort.Direction.ASC, "id")));

        Page<Restaurant> result = restaurantRepository.findFiltered(
                blankToNull(cuisine), minRating, blankToNull(search), dealsOnly, pageable);

        // Only the restaurants on THIS page are checked, one query each for deals and favorites
        List<String> ids = result.getContent().stream().map(Restaurant::getId).toList();
        Set<String> idsWithDeals = ids.isEmpty()
                ? Set.of()
                : new HashSet<>(menuItemRepository.findRestaurantIdsWithDeals(ids));
        Set<String> favoriteIds = ids.isEmpty()
                ? Set.of()
                : new HashSet<>(favoriteRepository.findFavoriteRestaurantIds(
                        currentCustomer.getCurrentCustomerId(), ids));

        List<RestaurantSummaryDto> items = result.getContent().stream()
                .map(r -> toSummary(r, idsWithDeals.contains(r.getId()), favoriteIds.contains(r.getId())))
                .toList();

        return PagedResponse.<RestaurantSummaryDto>builder()
                .items(items)
                .page(result.getNumber())
                .size(result.getSize())
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .build();
    }

    // An empty text box from the frontend ("") should count as "no filter"
    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private RestaurantSummaryDto toSummary(Restaurant r, boolean hasDeals, boolean isFavorite) {
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
                .isFavorite(isFavorite)
                .hasDeals(hasDeals)
                .build();
    }
}