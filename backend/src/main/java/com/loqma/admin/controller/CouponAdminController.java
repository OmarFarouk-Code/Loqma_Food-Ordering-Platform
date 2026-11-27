package com.loqma.coupon;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** Admin > Promotions (/admin/promotions). */
@RestController
@RequestMapping("/api/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
public class CouponAdminController {

    private final CouponService service;

    public CouponAdminController(CouponService service) { this.service = service; }

    /** Create coupon -> 201 + Location. */
    @PostMapping
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CouponRequest req) {
        CouponResponse created = service.create(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** List coupons with usage count + active state. Optional ?active=true|false. */
    @GetMapping
    public Page<CouponResponse> list(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(active, pageable);
    }

    @GetMapping("/{id}")
    public CouponResponse get(@PathVariable String id) { return service.get(id); }

    /** Edit coupon. 409 if already used. */
    @PutMapping("/{id}")
    public CouponResponse update(@PathVariable String id, @Valid @RequestBody CouponRequest req) {
        return service.update(id, req);
    }

    /** Activate. 409 if already used. */
    @PatchMapping("/{id}/activate")
    public CouponResponse activate(@PathVariable String id) { return service.setActive(id, true); }

    /** Deactivate. 409 if already used. */
    @PatchMapping("/{id}/deactivate")
    public CouponResponse deactivate(@PathVariable String id) { return service.setActive(id, false); }
}