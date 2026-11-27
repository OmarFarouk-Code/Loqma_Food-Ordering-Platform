package com.loqma.auth.service;

import com.loqma.auth.dto.LoginRequest;
import com.loqma.auth.dto.LoginResponse;
import com.loqma.auth.dto.RegisterRequest;
import com.loqma.auth.entity.User;
import com.loqma.auth.repository.DriverLookupRepository;
import com.loqma.auth.repository.UserRepository;
import com.loqma.common.enums.AccountStatus;
import com.loqma.common.enums.AddressLabel;
import com.loqma.common.enums.UserRole;
import com.loqma.customer.entity.Address;
import com.loqma.customer.entity.Customer;
import com.loqma.driver.entity.Driver;
import com.loqma.owner.entity.Restaurant;
import com.loqma.owner.entity.RestaurantOwner;
import com.loqma.owner.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{7,15}$");

    // Same rules as frontend/pages/auth/js/register.js
    private static final Pattern NATIONAL_ID = Pattern.compile("^\\d{14}$");
    private static final Pattern PLATE = Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N} -]{1,18}[\\p{L}\\p{N}]$");
    private static final Pattern LICENSE = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9\\-/ ]{2,48}[A-Za-z0-9]$");

    private static final int MAX_ADDRESSES = 5;
    private static final long LOGO_MAX_BYTES = 2L * 1024 * 1024;
    private static final long COVER_MAX_BYTES = 5L * 1024 * 1024;

    private final UserRepository userRepository;
    private final DriverLookupRepository driverLookup;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ImageStorageService imageStorageService;

    public AuthService(UserRepository userRepository, DriverLookupRepository driverLookup,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this(userRepository, driverLookup, passwordEncoder, jwtService, null);
    }

    @Autowired
    public AuthService(UserRepository userRepository, DriverLookupRepository driverLookup,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       ImageStorageService imageStorageService) {
        this.userRepository = userRepository;
        this.driverLookup = driverLookup;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.imageStorageService = imageStorageService;
    }

    // ------------------------------------------------------------------ register

    @Transactional
    public AccountStatus register(RegisterRequest req) {
        return register(req, null, null);
    }

    @Transactional
    public AccountStatus register(RegisterRequest req, MultipartFile logo, MultipartFile coverImage) {
        if (req == null) throw bad("Registration details are required");
        String name = clean(req.name());
        String email = normalizeEmail(req.email());
        String phone = clean(req.phone()).replaceAll("[\\s-]", "");
        String password = req.password() == null ? "" : req.password();
        UserRole role = req.role();

        if (name.isEmpty() || name.length() > 100) throw bad("Name is required (max 100 characters)");
        if (email.length() > 255 || !EMAIL.matcher(email).matches()) throw bad("Invalid email address");
        if (!PHONE.matcher(phone).matches()) throw bad("Invalid phone number");
        if (!isStrongPassword(password)) {
            throw bad("Password must be at least 12 characters and include a capital letter, a number and a symbol");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) throw bad("Password is too long");
        if (role == null || role == UserRole.ADMIN) throw bad("Invalid role");

        User user = switch (role) {
            case DRIVER -> buildDriver(req.driver());
            case RESTAURANT_OWNER -> buildOwner(req.restaurant());
            default -> buildCustomer(req.addresses());
        };

        if (userRepository.existsByEmail(email)) throw conflict("Email already in use");
        if (userRepository.existsByPhone(phone)) throw conflict("Phone number already in use");
        if (user instanceof Driver d) {
            if (driverLookup.existsByNationalId(d.getNationalId())) throw conflict("National ID already registered");
            if (driverLookup.existsByPlateNumber(d.getPlateNumber())) throw conflict("Plate number already registered");
            if (driverLookup.existsByDrivingLicense(d.getDrivingLicense())) throw conflict("Driving license already registered");
        }

        boolean hasLogo = logo != null && !logo.isEmpty();
        boolean hasCover = coverImage != null && !coverImage.isEmpty();
        if (hasLogo || hasCover) {
            if (!(user instanceof RestaurantOwner owner)) {
                throw bad("Images can only be uploaded for restaurant owner registration");
            }
            if (hasLogo) {
                owner.getRestaurant().setLogo(uploadRestaurantImage(logo, LOGO_MAX_BYTES, "restaurants/logos"));
            }
            if (hasCover) {
                owner.getRestaurant().setCoverImage(uploadRestaurantImage(coverImage, COVER_MAX_BYTES, "restaurants/covers"));
            }
        }

        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(role == UserRole.CUSTOMER ? AccountStatus.ACTIVE : AccountStatus.PENDING_APPROVAL);

        try {
            // Saves the users row plus the matching customers / drivers / restaurant_owners row,
            // and (through cascade) the customer's addresses or the owner's restaurant.
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw conflict("An account with these details already exists");
        }
        return user.getStatus();
    }

    // ------------------------------------------------------------------ login

    public LoginResponse login(LoginRequest req) {
        if (req.email() == null || req.password() == null) throw invalidCredentials();

        User user = userRepository.findByEmail(normalizeEmail(req.email()))
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(req.password(), user.getPassword())) throw invalidCredentials();

        switch (user.getStatus()) {
            case PENDING_APPROVAL -> throw forbidden("Your account is pending admin approval");
            case REJECTED -> {
                String reason = user.getRejectionReason();
                throw forbidden("Your account was rejected" + (reason == null || reason.isBlank() ? "" : ": " + reason));
            }
            case DEACTIVATED -> throw forbidden("Your account has been deactivated");
            case ACTIVE -> { }
        }
        return new LoginResponse(jwtService.generateToken(user), user.getRole().name());
    }

    // ------------------------------------------------------------------ builders

    private Customer buildCustomer(List<RegisterRequest.AddressRequest> addresses) {
        if (addresses == null || addresses.isEmpty()) throw bad("At least one delivery address is required");
        if (addresses.size() > MAX_ADDRESSES) throw bad("You can add up to " + MAX_ADDRESSES + " addresses");

        Customer customer = new Customer();
        boolean first = true;
        for (RegisterRequest.AddressRequest a : addresses) {
            if (a == null) throw bad("Address details are required");
            String nickname = clean(a.nickname());
            String street = clean(a.street());
            String building = clean(a.building());
            if (nickname.isEmpty()) throw bad("Address nickname is required");
            if (street.isEmpty() || street.length() > 150) throw bad("Street name is required (max 150 characters)");
            if (building.isEmpty() || building.length() > 100) throw bad("Building details are required (max 100 characters)");

            customer.getAddresses().add(Address.builder()
                    .customer(customer)
                    .label(toLabel(nickname))
                    .street(street)
                    .building(building)
                    .isDefault(first)          // first address is the default one
                    .build());
            first = false;
        }
        return customer;
    }

    private Driver buildDriver(RegisterRequest.DriverRequest d) {
        if (d == null) throw bad("Driver details are required");
        // Normalised the same way the frontend does, so duplicate checks work for direct API calls too
        String nid = clean(d.nationalId()).replaceAll("\\s", "");
        String plate = clean(d.plateNumber()).replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        String lic = clean(d.drivingLicense()).replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);

        if (!NATIONAL_ID.matcher(nid).matches()) throw bad("National ID must be 14 digits");
        if (plate.length() > 20 || !PLATE.matcher(plate).matches() || !plate.matches(".*\\d.*")) {
            throw bad("Invalid plate number");
        }
        if (lic.length() > 50 || !LICENSE.matcher(lic).matches()) throw bad("Invalid driving license number");

        Driver driver = new Driver();
        driver.setNationalId(nid);
        driver.setPlateNumber(plate);
        driver.setDrivingLicense(lic);
        return driver;
    }

    private RestaurantOwner buildOwner(RegisterRequest.RestaurantRequest r) {
        if (r == null) throw bad("Restaurant details are required");

        String restaurantName = clean(r.name());
        String branch = clean(r.branch());
        String cuisine = clean(r.cuisine());
        String address = clean(r.address());
        String phone = clean(r.phone()).replaceAll("[\\s-]", "");

        if (restaurantName.isEmpty() || restaurantName.length() > 100) throw bad("Restaurant name is required (max 100 characters)");
        if (branch.isEmpty() || branch.length() > 100) throw bad("Branch is required (max 100 characters)");
        if (cuisine.isEmpty() || cuisine.length() > 100) throw bad("Cuisine is required (max 100 characters)");
        if (address.isEmpty() || address.length() > 255) throw bad("Restaurant address is required (max 255 characters)");
        if (!phone.isEmpty() && !PHONE.matcher(phone).matches()) throw bad("Invalid restaurant phone number");

        String gps = resolveGps(r);

        RestaurantOwner owner = new RestaurantOwner();
        owner.setRestaurantName(restaurantName);
        owner.setBranch(branch);

        String logoUrl = clean(r.logo());
        String coverUrl = clean(r.coverimage());

        Restaurant restaurant = Restaurant.builder()
                .owner(owner)
                .name(restaurantName)
                .branch(branch)
                .cuisine(cuisine)
                .address(address)
                .phone(phone.isEmpty() ? null : phone)
                .logo(logoUrl.isEmpty() ? null : logoUrl)
                .coverImage(coverUrl.isEmpty() ? null : coverUrl)
                .gpsLocation(gps)
                .deliveryFee(number(r.deliveryFee(), 0.0, 0, 1000, "Delivery fee must be between 0 and 1000"))
                .minimumOrderAmount(number(r.minimumOrderAmount(), 0.0, 0, 10000, "Minimum order must be between 0 and 10000"))
                .prepTime(number(r.prepTime(), 25.0, 1, 240, "Preparation time must be between 1 and 240 minutes"))
                .maxDeliveryDistance(number(r.maxDeliveryDistance(), 15.0, 1, 100, "Max delivery distance must be between 1 and 100 km"))
                .build();

        owner.setRestaurant(restaurant);   // cascade = ALL on the owner saves the restaurants row too
        return owner;
    }

    // ------------------------------------------------------------------ helpers

    private String uploadRestaurantImage(MultipartFile file, long maxBytes, String folder) {
        if (file.getSize() > maxBytes) {
            throw bad("Image exceeds maximum allowed size of " + (maxBytes / (1024 * 1024)) + " MB");
        }
        if (imageStorageService == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Image upload service is not configured");
        }
        try {
            return imageStorageService.upload(file, folder);
        } catch (IllegalArgumentException e) {
            throw bad(e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image");
        }
    }

    /** "lat,lng" string stored in restaurants.gps_location. Required, and must be a real coordinate. */
    private String resolveGps(RegisterRequest.RestaurantRequest r) {
        Double lat = r.latitude();
        Double lng = r.longitude();

        if (lat == null || lng == null) {
            // fall back to the "lat,lng" string the frontend also sends
            String raw = clean(r.gpsLocation());
            String[] parts = raw.split(",");
            if (parts.length != 2) throw bad("Restaurant location is required");
            try {
                lat = Double.parseDouble(parts[0].trim());
                lng = Double.parseDouble(parts[1].trim());
            } catch (NumberFormatException e) {
                throw bad("Restaurant location is invalid");
            }
        }
        if (!Double.isFinite(lat) || !Double.isFinite(lng)
                || lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw bad("Restaurant location is invalid");
        }
        return String.format(Locale.ROOT, "%.6f,%.6f", lat, lng);
    }

    /** Null -> default; otherwise must be a finite number inside [min, max]. */
    private double number(Double value, double defaultValue, double min, double max, String message) {
        if (value == null) return defaultValue;
        if (!Double.isFinite(value) || value < min || value > max) throw bad(message);
        return value;
    }

    /** Frontend lets people type any nickname; the database only knows HOME / WORK / GYM / OTHER. */
    private AddressLabel toLabel(String nickname) {
        try {
            return AddressLabel.valueOf(nickname.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return AddressLabel.OTHER;
        }
    }

    private String normalizeEmail(String s) { return clean(s).toLowerCase(Locale.ROOT); }

    private String clean(String s) { return s == null ? "" : s.trim(); }

    private boolean isStrongPassword(String p) {
        return p.length() >= 12 && p.matches(".*[A-Z].*") && p.matches(".*[0-9].*") && p.matches(".*[^A-Za-z0-9].*");
    }

    private ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private ResponseStatusException conflict(String m) { return new ResponseStatusException(HttpStatus.CONFLICT, m); }
    private ResponseStatusException forbidden(String m) { return new ResponseStatusException(HttpStatus.FORBIDDEN, m); }
    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
    public java.util.Map<String, String> me(String userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(this::invalidCredentials);
        return java.util.Map.of("name", u.getName(), "email", u.getEmail(),
                "role", u.getRole().name(), "status", u.getStatus().name());
    }
}