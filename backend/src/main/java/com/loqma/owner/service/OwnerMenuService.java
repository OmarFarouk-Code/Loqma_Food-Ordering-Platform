package com.loqma.owner.service;

import com.loqma.owner.dto.MenuDtos.*;
import com.loqma.owner.entity.ItemExtra;
import com.loqma.owner.entity.ItemSize;
import com.loqma.owner.entity.MenuItem;
import com.loqma.owner.exception.BadRequestException;
import com.loqma.owner.exception.ResourceNotFoundException;
import com.loqma.owner.repository.OwnerMenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerMenuService {

    private final OwnerMenuItemRepository menuItemRepository;
    private final CurrentOwnerProvider currentOwner;

    @Transactional(readOnly = true)
    public List<MenuItemResponse> list(String search, String stock) {
        String restaurantId = currentOwner.getRestaurant().getId();
        String q = search == null ? "" : search.trim().toLowerCase();
        String filter = stock == null ? "all" : stock.trim().toLowerCase();

        return menuItemRepository.findByRestaurantIdOrderByCategoryAscNameAsc(restaurantId).stream()
                .filter(i -> q.isEmpty() || i.getName().toLowerCase().contains(q))
                .filter(i -> switch (filter) {
                    case "in" -> Boolean.TRUE.equals(i.getIsAvailable());
                    case "out" -> !Boolean.TRUE.equals(i.getIsAvailable());
                    default -> true;
                })
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MenuItemResponse create(MenuItemRequest req) {
        validateSizes(req);
        MenuItem item = MenuItem.builder()
                .restaurant(currentOwner.getRestaurant())
                .build();
        apply(item, req);
        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse update(String id, MenuItemRequest req) {
        validateSizes(req);
        MenuItem item = findOwned(id);
        apply(item, req);
        return toResponse(menuItemRepository.save(item));
    }

    @Transactional
    public void delete(String id) {
        MenuItem item = findOwned(id);
        menuItemRepository.delete(item);
        menuItemRepository.flush(); // forces the database check now, so a cart/order conflict becomes a 409
    }

    @Transactional
    public MenuItemResponse setStock(String id, boolean available) {
        MenuItem item = findOwned(id);
        item.setIsAvailable(available);
        return toResponse(menuItemRepository.save(item));
    }

    // ---------- helpers ----------

    private MenuItem findOwned(String id) {
        String restaurantId = currentOwner.getRestaurant().getId();
        return menuItemRepository.findByIdAndRestaurantId(id, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
    }

    /** Spec: larger sizes must cost more than the base price. */
    private void validateSizes(MenuItemRequest req) {
        if (req.sizes() == null) return;
        for (SizeRequest s : req.sizes()) {
            if (s.price() <= req.basePrice()) {
                throw new BadRequestException("Size '" + s.sizeName()
                        + "' must cost more than the base price (" + req.basePrice() + ")");
            }
        }
    }

    private void apply(MenuItem item, MenuItemRequest req) {
        item.setCategory(req.category().trim());
        item.setName(req.name().trim());
        item.setDescription(req.description());
        item.setBasePrice(req.basePrice());
        item.setPhoto(req.photo());
        syncSizes(item, req.sizes());
        syncExtras(item, req.extras());
    }

    /** Keeps existing rows (matched by id) so carts that reference them don't break. */
    private void syncSizes(MenuItem item, List<SizeRequest> requests) {
        Map<String, ItemSize> existing = item.getSizes().stream()
                .filter(s -> s.getId() != null)
                .collect(Collectors.toMap(ItemSize::getId, Function.identity()));

        List<ItemSize> result = new ArrayList<>();
        if (requests != null) {
            for (SizeRequest r : requests) {
                ItemSize size = r.id() == null ? null : existing.get(r.id());
                if (size == null) {
                    size = new ItemSize();
                    size.setMenuItem(item);
                }
                size.setSizeName(r.sizeName().trim());
                size.setPrice(r.price());
                result.add(size);
            }
        }
        item.getSizes().clear();
        item.getSizes().addAll(result);
    }

    private void syncExtras(MenuItem item, List<ExtraRequest> requests) {
        Map<String, ItemExtra> existing = item.getExtras().stream()
                .filter(e -> e.getId() != null)
                .collect(Collectors.toMap(ItemExtra::getId, Function.identity()));

        List<ItemExtra> result = new ArrayList<>();
        if (requests != null) {
            for (ExtraRequest r : requests) {
                ItemExtra extra = r.id() == null ? null : existing.get(r.id());
                if (extra == null) {
                    extra = new ItemExtra();
                    extra.setMenuItem(item);
                }
                extra.setName(r.name().trim());
                extra.setPrice(r.price());
                result.add(extra);
            }
        }
        item.getExtras().clear();
        item.getExtras().addAll(result);
    }

    private MenuItemResponse toResponse(MenuItem i) {
        return new MenuItemResponse(
                i.getId(),
                i.getCategory(),
                i.getName(),
                i.getDescription(),
                i.getBasePrice(),
                i.getPhoto(),
                Boolean.TRUE.equals(i.getIsAvailable()),
                i.getSizes().stream()
                        .map(s -> new SizeResponse(s.getId(), s.getSizeName(), s.getPrice())).toList(),
                i.getExtras().stream()
                        .map(e -> new ExtraResponse(e.getId(), e.getName(), e.getPrice())).toList());
    }
}