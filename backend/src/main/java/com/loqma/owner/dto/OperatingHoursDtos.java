package com.loqma.owner.dto;

import com.loqma.common.enums.DayOfWeek;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class OperatingHoursDtos {

    private OperatingHoursDtos() {}

    public record HoursItemRequest(
            DayOfWeek dayOfWeek,
            LocalTime openingTime,
            LocalTime closingTime,
            @NotNull Boolean isDayOff,
            @NotNull Boolean is24Hours,
            @NotNull Boolean isSpecialDay,
            LocalDate specialDate) {}

    public record ScheduleUpdateRequest(
            @Valid @NotNull List<HoursItemRequest> hours) {}

    public record StoreStatusRequest(
            @NotNull Boolean open) {}

    public record HoursItemResponse(
            String id,
            DayOfWeek dayOfWeek,
            LocalTime openingTime,
            LocalTime closingTime,
            boolean isDayOff,
            boolean is24Hours,
            boolean isSpecialDay,
            LocalDate specialDate) {}

    public record ScheduleResponse(
            boolean open,
            List<HoursItemResponse> hours) {}
}