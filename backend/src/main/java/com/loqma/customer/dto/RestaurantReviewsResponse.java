package com.loqma.customer.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class RestaurantReviewsResponse {
    private String restaurantId;
    private Double averageRating;            
    private long totalReviews;
    private Map<Integer, Long> ratingCounts; 
    private PagedResponse<ReviewDto> reviews;
}