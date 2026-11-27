package com.loqma.auth;

import com.loqma.auth.dto.LoginRequest;
import com.loqma.auth.dto.LoginResponse;
import com.loqma.auth.dto.RegisterRequest;
import com.loqma.auth.repository.DriverLookupRepository;
import com.loqma.auth.repository.UserRepository;
import com.loqma.auth.service.AuthService;
import com.loqma.auth.service.JwtService;
import com.loqma.common.enums.AccountStatus;
import com.loqma.common.enums.UserRole;
import com.loqma.customer.entity.Customer;
import com.loqma.owner.entity.RestaurantOwner;
import com.loqma.owner.service.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DriverLookupRepository driverLookup;
    @Mock
    private ImageStorageService imageStorageService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("test-secret-that-is-at-least-32-characters-long", 3600000L);
        authService = new AuthService(userRepository, driverLookup, passwordEncoder, jwtService, imageStorageService);
    }

    @Test
    void registerCustomerCreatesActiveCustomerWithNormalizedEmail() {
        RegisterRequest req = new RegisterRequest(
                "Omar Aly", "  OMAR@Example.com ", "0100 000 0001", "StrongPass#123",
                UserRole.CUSTOMER,
                List.of(new RegisterRequest.AddressRequest("Home", "Tahrir St", "Bldg 14")),
                null, null);

        AccountStatus status = authService.register(req);
        assertEquals(AccountStatus.ACTIVE, status);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(userRepository).saveAndFlush(captor.capture());
        Customer saved = captor.getValue();
        assertEquals("omar@example.com", saved.getEmail());
        assertEquals("01000000001", saved.getPhone());
        assertEquals(1, saved.getAddresses().size());
        assertTrue(saved.getAddresses().get(0).getIsDefault());
    }

    @Test
    void registerOwnerWithImagesUploadsAndSavesUrls() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "Owner", "owner@example.com", "01000000002", "StrongPass#123",
                UserRole.RESTAURANT_OWNER, List.of(), null,
                new RegisterRequest.RestaurantRequest(
                        "Koshary", null, "Downtown", "Egyptian", "12 Tahrir",
                        null, "30.044400,31.235700", 30.0444, 31.2357,
                        15.0, 50.0, 25.0, 10.0, null, null));

        MockMultipartFile logo = new MockMultipartFile("logo", "logo.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2});
        MockMultipartFile cover = new MockMultipartFile("coverImage", "cover.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{3, 4});
        when(imageStorageService.upload(logo, "restaurants/logos")).thenReturn("https://cdn.example/logo.png");
        when(imageStorageService.upload(cover, "restaurants/covers")).thenReturn("https://cdn.example/cover.jpg");

        AccountStatus status = authService.register(req, logo, cover);
        assertEquals(AccountStatus.PENDING_APPROVAL, status);

        ArgumentCaptor<RestaurantOwner> captor = ArgumentCaptor.forClass(RestaurantOwner.class);
        verify(userRepository).saveAndFlush(captor.capture());
        assertEquals("https://cdn.example/logo.png", captor.getValue().getRestaurant().getLogo());
        assertEquals("https://cdn.example/cover.jpg", captor.getValue().getRestaurant().getCoverImage());
    }

    @Test
    void registerRejectsDuplicateDriverNationalIdWith409() {
        RegisterRequest req = new RegisterRequest(
                "Driver", "driver@example.com", "01000000003", "StrongPass#123",
                UserRole.DRIVER, List.of(),
                new RegisterRequest.DriverRequest(null, null, null, "29801010000000", "ABC 1234", "DL-12345"),
                null);
        when(driverLookup.existsByNationalId("29801010000000")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.register(req));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void loginRejectsPendingUserWith403AndAllowsActiveUser() {
        Customer pending = new Customer();
        pending.setId("u1");
        pending.setEmail("u@example.com");
        pending.setPassword(passwordEncoder.encode("StrongPass#123"));
        pending.setRole(UserRole.DRIVER);
        pending.setStatus(AccountStatus.PENDING_APPROVAL);

        when(userRepository.findByEmail("u@example.com")).thenReturn(Optional.of(pending));
        ResponseStatusException pendingEx = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("u@example.com", "StrongPass#123")));
        assertEquals(HttpStatus.FORBIDDEN, pendingEx.getStatusCode());

        pending.setStatus(AccountStatus.ACTIVE);
        LoginResponse res = authService.login(new LoginRequest("u@example.com", "StrongPass#123"));
        assertNotNull(res.token());
        assertEquals("DRIVER", res.role());
        assertEquals("u1", jwtService.parseToken(res.token()).getSubject());
    }
}
