package com.loqma.admin.service;
package com.loqma.admin.service;

import com.loqma.admin.entity.Coupon;
import com.loqma.coupon.CouponRepository;
import com.loqma.coupon.CouponRequest;
import com.loqma.coupon.CouponResponse;
import com.loqma.coupon.exception.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class CouponService {

    private final AdminCouponRepository repo;

    public CouponService(AdminCouponRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public CouponResponse create(CouponRequest req) {
        String code = normalize(req.code());
        if (repo.existsByCodeIgnoreCase(code)) throw new DuplicateCouponCodeException(code);

        Coupon c = new Coupon();
        apply(c, req, code);
        c.setIsActive(true);
        return CouponResponse.from(repo.save(c));
    }

    @Transactional(readOnly = true)
    public Page<CouponResponse> list(Boolean active, Pageable pageable) {
        Page<Coupon> page = (active == null) ? repo.findAll(pageable) : repo.findByIsActive(active, pageable);
        return page.map(CouponResponse::from);
    }

    @Transactional(readOnly = true)
    public CouponResponse get(String id) {
        return CouponResponse.from(find(id));
    }

    @Transactional
    public CouponResponse update(String id, CouponRequest req) {
        Coupon c = repo.findByIdForUpdate(id).orElseThrow(() -> new CouponNotFoundException(id));
        assertNotUsed(c, "edited");

        String code = normalize(req.code());
        if (repo.existsByCodeIgnoreCaseAndIdNot(code, id)) throw new DuplicateCouponCodeException(code);

        apply(c, req, code);
        return CouponResponse.from(c);
    }

    @Transactional
    public CouponResponse setActive(String id, boolean active) {
        Coupon c = repo.findByIdForUpdate(id).orElseThrow(() -> new CouponNotFoundException(id));
        if (Boolean.TRUE.equals(c.getIsActive()) == active) return CouponResponse.from(c);
        assertNotUsed(c, active ? "activated" : "deactivated");
        c.setIsActive(active);
        return CouponResponse.from(c);
    }

    // ---- helpers ----

    private Coupon find(String id) {
        return repo.findById(id).orElseThrow(() -> new CouponNotFoundException(id));
    }

    private void assertNotUsed(Coupon c, String action) {
        if (c.isUsed()) throw new CouponLockedException(c.getCode(), action);
    }

    private void apply(Coupon c, CouponRequest r, String code) {
        c.setName(r.name().trim());
        c.setCode(code);
        c.setDiscountAmount(r.discountAmount().doubleValue());
        c.setExpiryDate(r.expiryDate());
        c.setMinimumOrderAmount(r.minimumOrderAmount().doubleValue());
    }

    private String normalize(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
