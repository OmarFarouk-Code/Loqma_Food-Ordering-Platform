package com.loqma.owner.controller;

import com.loqma.owner.dto.OperatingHoursDtos.*;
import com.loqma.owner.service.OwnerOperatingHoursService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/owner/operating-hours")
@RequiredArgsConstructor
public class OwnerOperatingHoursController {

    private final OwnerOperatingHoursService operatingHoursService;
    @GetMapping
    public ScheduleResponse get() {
        return operatingHoursService.getSchedule();
    }

    @PutMapping
    public ScheduleResponse update(@Valid @RequestBody ScheduleUpdateRequest request) {
        return operatingHoursService.updateSchedule(request);
    }

    @PatchMapping("/status")
    public Map<String, Boolean> updateStatus(@Valid @RequestBody StoreStatusRequest request) {
        boolean isOpen = operatingHoursService.updateStoreStatus(request);
        return Map.of("open", isOpen);
    }
}