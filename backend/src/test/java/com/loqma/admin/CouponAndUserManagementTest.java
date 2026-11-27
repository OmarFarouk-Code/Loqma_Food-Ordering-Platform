package com.loqma.admin;

import com.loqma.admin.controller.CouponAdminController;
import com.loqma.admin.controller.UserManagementController;
import com.loqma.admin.repository.AdminUserRepository;
import com.loqma.admin.service.CouponService;
import com.loqma.admin.service.UserManagementService;
import com.loqma.auth.entity.User;
import com.loqma.common.enums.AccountStatus;
import com.loqma.common.enums.UserRole;
import com.loqma.coupon.AdminCouponRepository;
import com.loqma.coupon.CouponRequest;
import com.loqma.coupon.CouponResponse;
import com.loqma.coupon.exception.CouponLockedException;
import com.loqma.customer.entity.Customer;
import com.loqma.order.entity.Coupon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CouponAndUserManagementTest {

    @Mock
    private AdminCouponRepository couponRepository;
    @Mock
    private AdminUserRepository adminUserRepository;

    private CouponService couponService;
    private MockMvc userMgmtMvc;
    private MockMvc couponMvc;

    @BeforeEach
    void setUp() {
        couponService = new CouponService(couponRepository);
        UserManagementService userManagementService = new UserManagementService(adminUserRepository);
        userMgmtMvc = MockMvcBuilders.standaloneSetup(new UserManagementController(userManagementService)).build();
        couponMvc = MockMvcBuilders.standaloneSetup(new CouponAdminController(couponService)).build();
    }

    @Test
    void createCouponNormalizesCodeAndMapsFields() {
        when(couponRepository.existsByCodeIgnoreCase("WELCOME30")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> {
            Coupon c = inv.getArgument(0);
            c.setId("c-1");
            return c;
        });

        CouponResponse res = couponService.create(new CouponRequest(
                "Welcome", " welcome30 ", new BigDecimal("30.00"),
                LocalDate.now().plusDays(10), new BigDecimal("150.00")));

        assertEquals("c-1", res.id());
        assertEquals("WELCOME30", res.code());
        assertEquals(0, new BigDecimal("30.0").compareTo(res.discountAmount()));
        assertTrue(res.active());
    }

    @Test
    void deactivateUsedCouponThrowsLocked() {
        Coupon used = Coupon.builder()
                .id("c-2")
                .code("USED10")
                .discountAmount(10.0)
                .minimumOrderAmount(0.0)
                .expiryDate(LocalDate.now().plusDays(5))
                .isActive(true)
                .usageCount(1)
                .build();
        when(couponRepository.findByIdForUpdate("c-2")).thenReturn(Optional.of(used));

        assertThrows(CouponLockedException.class, () -> couponService.setActive("c-2", false));
    }

    @Test
    void userManagementListsAndTogglesUsersByStringId() throws Exception {
        Customer customer = new Customer();
        customer.setId("cust-1");
        customer.setName("Omar Aly");
        customer.setEmail("omar@example.com");
        customer.setPhone("01000000001");
        customer.setRole(UserRole.CUSTOMER);
        customer.setStatus(AccountStatus.ACTIVE);

        when(adminUserRepository.findAll()).thenReturn(List.<User>of(customer));
        when(adminUserRepository.findById("cust-1")).thenReturn(Optional.of(customer));
        when(adminUserRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userMgmtMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cust-1"))
                .andExpect(jsonPath("$[0].name").value("Omar Aly"));

        userMgmtMvc.perform(patch("/api/admin/users/cust-1/toggle-active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEACTIVATED"));
    }

    @Test
    void couponControllerGetsSingleCoupon() throws Exception {
        Coupon coupon = Coupon.builder()
                .id("c-3")
                .name("Save 20")
                .code("SAVE20")
                .discountAmount(20.0)
                .minimumOrderAmount(100.0)
                .expiryDate(LocalDate.now().plusDays(7))
                .isActive(true)
                .usageCount(0)
                .build();
        when(couponRepository.findById("c-3")).thenReturn(Optional.of(coupon));

        couponMvc.perform(get("/api/admin/coupons/c-3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SAVE20"))
                .andExpect(jsonPath("$.active").value(true));
    }
}
