package com.loqma.owner.service;

import com.loqma.owner.entity.Restaurant;
import com.loqma.owner.exception.ResourceNotFoundException;
import com.loqma.owner.repository.OwnerRepository;
import com.loqma.owner.repository.OwnerRestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the authenticated restaurant owner when logged in, falling back to
 * the seeded test owner when unauthenticated during local development.
 */
@Component
@RequiredArgsConstructor
public class CurrentOwnerProvider {

    public static final String FAKE_OWNER_EMAIL = "owner@loqma.test";

    private final OwnerRepository ownerRepository;
    private final OwnerRestaurantRepository restaurantRepository;

    public String getOwnerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof String userId
                && !"anonymousUser".equals(userId)
                && ownerRepository.existsById(userId)) {
            return userId;
        }
        return ownerRepository.findByEmail(FAKE_OWNER_EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Test owner not found. Restart the backend so the seeder runs."))
                .getId();
    }

    public Restaurant getRestaurant() {
        return restaurantRepository.findByOwnerId(getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant found for this owner"));
    }
}