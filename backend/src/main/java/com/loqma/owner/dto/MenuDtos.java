package com.loqma.owner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class MenuDtos {

    private MenuDtos() {}

    // ----- requests (what the frontend sends) -----

    /** id is optional: send it for an existing size, leave it out for a new one. */
    public record SizeRequest(
            String id,
            @NotBlank @Size(max = 50) String sizeName,
            @NotNull @Positive Double price) {}

    public record ExtraRequest(
            String id,
            @NotBlank @Size(max = 100) String name,
            @NotNull @PositiveOrZero Double price) {}

    public record MenuItemRequest(
            @NotBlank @Size(max = 50) String category,
            @NotBlank @Size(max = 100) String name,
            String description,
            @NotNull @Positive Double basePrice,
            @Size(max = 500) String photo,
            @Valid List<SizeRequest> sizes,
            @Valid List<ExtraRequest> extras) {}

    public record StockUpdateRequest(@NotNull Boolean available) {}

    // ----- responses (what the backend returns) -----

    public record SizeResponse(String id, String sizeName, Double price) {}

    public record ExtraResponse(String id, String name, Double price) {}

    public record MenuItemResponse(
            String id,
            String category,
            String name,
            String description,
            Double basePrice,
            String photo,
            boolean available,
            List<SizeResponse> sizes,
            List<ExtraResponse> extras) {}
}