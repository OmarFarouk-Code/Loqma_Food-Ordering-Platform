package com.loqma.customer.service;

import com.loqma.customer.dto.PagedResponse;
import com.loqma.customer.dto.RestaurantReviewsResponse;
import com.loqma.customer.dto.ReviewDto;
import com.loqma.customer.entity.Review;
import com.loqma.customer.repository.CustomerRestaurantRepository;
import com.loqma.customer.repository.CustomerReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerReviewService {

    private static final int MAX_PAGE_SIZE = 50;

    private final CustomerReviewRepository reviewRepository;
    private final CustomerRestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public RestaurantReviewsResponse getReviews(String restaurantId, int page, int size) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
                    .and(Sort.by(Sort.Direction.ASC, "id")));

        Page<Review> result = reviewRepository.findByRestaurant(restaurantId, pageable);

        List<ReviewDto> items = result.getContent().stream().map(this::toDto).toList();

        PagedResponse<ReviewDto> reviews = PagedResponse.<ReviewDto>builder()
                .items(items)
                .page(result.getNumber())
                .size(result.getSize())
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .build();

        Double average = reviewRepository.findAverageRating(restaurantId);

        return RestaurantReviewsResponse.builder()
                .restaurantId(restaurantId)
                .averageRating(average == null ? null : Math.round(average * 10) / 10.0)
                .totalReviews(result.getTotalElements())
                .ratingCounts(buildRatingCounts(restaurantId))
                .reviews(reviews)
                .build();
    }

   
    private Map<Integer, Long> buildRatingCounts(String restaurantId) {
        Map<Integer, Long> counts = new LinkedHashMap<>();
        for (int star = 1; star <= 5; star++) {
            counts.put(star, 0L);
        }
        for (Object[] row : reviewRepository.countByRating(restaurantId)) {
            counts.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    private ReviewDto toDto(Review r) {
        return ReviewDto.builder()
                .id(r.getId())
                .customerName(shortName(r.getCustomerName()))
                .rating(r.getRestaurantRating())
                .comment(r.getRestaurantComment())
                .createdAt(r.getCreatedAt())
                .build();
    }

   
    private String shortName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Customer";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0];
        }
        return parts[0] + " " + Character.toUpperCase(parts[parts.length - 1].charAt(0)) + ".";
    }
}