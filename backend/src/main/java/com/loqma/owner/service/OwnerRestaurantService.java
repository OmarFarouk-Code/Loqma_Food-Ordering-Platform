package com.loqma.owner.service;

import com.loqma.owner.dto.RestaurantDtos.ProfileRequest;
import com.loqma.owner.dto.RestaurantDtos.ProfileResponse;
import com.loqma.owner.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerRestaurantService {

    private final CurrentOwnerProvider currentOwner;
    private final com.loqma.owner.repository.OwnerRestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public ProfileResponse get() {
        return toResponse(currentOwner.getRestaurant());
    }

    @Transactional
    public ProfileResponse update(ProfileRequest req) {
        Restaurant r = currentOwner.getRestaurant();
        r.setName(req.name().trim());
        r.setBranch(req.branch().trim());
        r.setCuisine(req.cuisine().trim());
        r.setLogo(req.logo());
        r.setCoverImage(req.coverImage());
        r.setAddress(req.address());
        r.setPhone(req.phone());
        r.setDeliveryFee(req.deliveryFee());
        r.setMinimumOrderAmount(req.minimumOrderAmount());
        r.setGpsLocation(req.gpsLocation());

        // RestaurantOwner stores a duplicate copy of name and branch: keep it in sync
        r.getOwner().setRestaurantName(r.getName());
        r.getOwner().setBranch(r.getBranch());

        return toResponse(restaurantRepository.save(r));
    }

    private ProfileResponse toResponse(Restaurant r) {
        return new ProfileResponse(
                r.getId(), r.getName(), r.getBranch(), r.getCuisine(), r.getLogo(),
                r.getCoverImage(), r.getAddress(), r.getPhone(), r.getDeliveryFee(),
                r.getMinimumOrderAmount(), r.getAverageRating(),
                Boolean.TRUE.equals(r.getIsOpen()), r.getGpsLocation());
    }
}