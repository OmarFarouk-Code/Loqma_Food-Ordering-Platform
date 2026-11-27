package com.loqma.owner.service;

import com.loqma.owner.entity.Restaurant;
import com.loqma.owner.exception.ResourceNotFoundException;
import com.loqma.owner.repository.OwnerRepository;
import com.loqma.owner.repository.OwnerRestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * TEMPORARY: always returns the seeded test owner.
 * When Team 3's login is merged, change ONLY getOwnerId() to read the real logged-in user.
 */
@Component
@RequiredArgsConstructor
public class CurrentOwnerProvider {

    public static final String FAKE_OWNER_EMAIL = "owner@loqma.test";

    private final OwnerRepository ownerRepository;
    private final OwnerRestaurantRepository restaurantRepository;

    public String getOwnerId() {
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