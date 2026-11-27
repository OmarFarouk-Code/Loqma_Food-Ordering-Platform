package com.loqma.owner.service;

import com.loqma.common.enums.DayOfWeek;
import com.loqma.owner.dto.OperatingHoursDtos.*;
import com.loqma.owner.entity.OperatingHours;
import com.loqma.owner.entity.Restaurant;
import com.loqma.owner.repository.OwnerRestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerOperatingHoursService {

    private final CurrentOwnerProvider currentOwner;
    private final OwnerRestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public ScheduleResponse getSchedule() {
        return toResponse(currentOwner.getRestaurant());
    }

    @Transactional
    public ScheduleResponse updateSchedule(ScheduleUpdateRequest request) {
        Restaurant r = currentOwner.getRestaurant();

        // orphanRemoval = true on Restaurant.operatingHours deletes previous entries cleanly
        r.getOperatingHours().clear();

        for (HoursItemRequest item : request.hours()) {
            DayOfWeek resolvedDay = item.dayOfWeek();
            if (resolvedDay == null && item.specialDate() != null) {
                resolvedDay = DayOfWeek.valueOf(item.specialDate().getDayOfWeek().name());
            }

            OperatingHours entity = OperatingHours.builder()
                    .restaurant(r)
                    .dayOfWeek(resolvedDay)
                    .openingTime(item.openingTime())
                    .closingTime(item.closingTime())
                    .isDayOff(Boolean.TRUE.equals(item.isDayOff()))
                    .is24Hours(Boolean.TRUE.equals(item.is24Hours()))
                    .isSpecialDay(Boolean.TRUE.equals(item.isSpecialDay()))
                    .specialDate(item.specialDate())
                    .build();

            r.getOperatingHours().add(entity);
        }

        return toResponse(restaurantRepository.save(r));
    }

    @Transactional
    public boolean updateStoreStatus(StoreStatusRequest request) {
        Restaurant r = currentOwner.getRestaurant();
        r.setIsOpen(request.open());
        restaurantRepository.save(r);
        return Boolean.TRUE.equals(r.getIsOpen());
    }

    private ScheduleResponse toResponse(Restaurant r) {
        List<HoursItemResponse> items = r.getOperatingHours().stream()
                .map(h -> new HoursItemResponse(
                        h.getId(),
                        h.getDayOfWeek(),
                        h.getOpeningTime(),
                        h.getClosingTime(),
                        Boolean.TRUE.equals(h.getIsDayOff()),
                        Boolean.TRUE.equals(h.getIs24Hours()),
                        Boolean.TRUE.equals(h.getIsSpecialDay()),
                        h.getSpecialDate()
                ))
                .toList();

        return new ScheduleResponse(Boolean.TRUE.equals(r.getIsOpen()), items);
    }
}