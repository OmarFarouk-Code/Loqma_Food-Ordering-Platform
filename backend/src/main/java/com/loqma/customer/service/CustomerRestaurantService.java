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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerRestaurantService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_TIME_FILTER_SCAN = 1000;

    private final CustomerRestaurantRepository restaurantRepository;
    private final CustomerMenuItemRepository menuItemRepository;
    private final CustomerFavoriteRepository favoriteRepository;
    private final CurrentCustomerProvider currentCustomer;
    private final DeliveryTimeCalculator deliveryTimeCalculator;

    public PagedResponse<RestaurantSummaryDto> listRestaurants(String cuisine, Double minRating,
                                                               String search, boolean dealsOnly,
                                                               Double lat, Double lng,
                                                               Integer maxDeliveryTime,
                                                               int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Sort sort = Sort.by(Sort.Direction.DESC, "averageRating")
                .and(Sort.by(Sort.Direction.ASC, "id"));

        String cleanCuisine = blankToNull(cuisine);
        String cleanSearch = blankToNull(search);
        boolean timeFilter = maxDeliveryTime != null && lat != null && lng != null;

        if (!timeFilter) {
            // Normal path: the database does the paging
            Page<Restaurant> result = restaurantRepository.findFiltered(
                    cleanCuisine, minRating, cleanSearch, dealsOnly,
                    PageRequest.of(safePage, safeSize, sort));
            return buildResponse(result.getContent(), lat, lng, safePage, safeSize,
                    result.getTotalElements(), result.getTotalPages(), result.hasNext());
        }

        // Time-filter path: SQL can't compute travel time, so filter in Java, then page by hand
        List<Restaurant> candidates = restaurantRepository.findFiltered(
                cleanCuisine, minRating, cleanSearch, dealsOnly,
                PageRequest.of(0, MAX_TIME_FILTER_SCAN, sort)).getContent();

        List<Restaurant> withinTime = candidates.stream()
                .filter(r -> {
                    Integer minutes = deliveryTimeCalculator.estimateMinutes(
                            r.getGpsLocation(), r.getPrepTime(), lat, lng);
                    return minutes != null && minutes <= maxDeliveryTime;
                })
                .toList();

        int from = (int) Math.min((long) safePage * safeSize, withinTime.size());
        int to = Math.min(from + safeSize, withinTime.size());
        int totalPages = (int) Math.ceil((double) withinTime.size() / safeSize);

        return buildResponse(withinTime.subList(from, to), lat, lng, safePage, safeSize,
                withinTime.size(), totalPages, to < withinTime.size());
    }

    private PagedResponse<RestaurantSummaryDto> buildResponse(List<Restaurant> restaurants,
                                                              Double lat, Double lng,
                                                              int page, int size,
                                                              long totalItems, int totalPages,
                                                              boolean hasNext) {
        // Only the restaurants on THIS page are checked, one query each for deals and favorites
        List<String> ids = restaurants.stream().map(Restaurant::getId).toList();
        Set<String> idsWithDeals = ids.isEmpty()
                ? Set.of()
                : new HashSet<>(menuItemRepository.findRestaurantIdsWithDeals(ids));
        Set<String> favoriteIds = ids.isEmpty()
                ? Set.of()
                : new HashSet<>(favoriteRepository.findFavoriteRestaurantIds(
                        currentCustomer.getCurrentCustomerId(), ids));

        List<RestaurantSummaryDto> items = restaurants.stream()
                .map(r -> toSummary(r, idsWithDeals.contains(r.getId()),
                        favoriteIds.contains(r.getId()), lat, lng))
                .toList();

        return PagedResponse.<RestaurantSummaryDto>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .build();
    }

    // An empty text box from the frontend ("") should count as "no filter"
    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private RestaurantSummaryDto toSummary(Restaurant r, boolean hasDeals, boolean isFavorite,
                                           Double lat, Double lng) {
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
                .deliveryTimeMinutes(deliveryTimeCalculator.estimateMinutes(
                        r.getGpsLocation(), r.getPrepTime(), lat, lng))
                .isOpen(r.getIsOpen())
                .isFavorite(isFavorite)
                .hasDeals(hasDeals)
                .build();
    }
}