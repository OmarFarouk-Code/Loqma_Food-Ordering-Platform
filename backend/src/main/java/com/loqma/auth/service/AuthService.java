package com.loqma.auth.service;

import com.loqma.auth.dto.LoginRequest;
import com.loqma.auth.dto.LoginResponse;
import com.loqma.auth.dto.RegisterRequest;
import com.loqma.auth.entity.User;
import com.loqma.auth.repository.DriverLookupRepository;
import com.loqma.auth.repository.UserRepository;
import com.loqma.common.enums.AccountStatus;
import com.loqma.common.enums.UserRole;
import com.loqma.customer.entity.Customer;
import com.loqma.driver.entity.Driver;
import com.loqma.owner.entity.RestaurantOwner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{7,15}$");

    private final UserRepository userRepository;
    private final DriverLookupRepository driverLookup;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, DriverLookupRepository driverLookup,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.driverLookup = driverLookup;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AccountStatus register(RegisterRequest req) {
        String name = clean(req.name());
        String email = clean(req.email());
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
            default -> new Customer();
        };

        if (userRepository.existsByEmail(email)) throw conflict("Email already in use");
        if (userRepository.existsByPhone(phone)) throw conflict("Phone number already in use");
        if (user instanceof Driver d) {
            if (driverLookup.existsByNationalId(d.getNationalId())) throw conflict("National ID already registered");
            if (driverLookup.existsByPlateNumber(d.getPlateNumber())) throw conflict("Plate number already registered");
            if (driverLookup.existsByDrivingLicense(d.getDrivingLicense())) throw conflict("Driving license already registered");
        }

        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(role == UserRole.CUSTOMER ? AccountStatus.ACTIVE : AccountStatus.PENDING_APPROVAL);

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw conflict("An account with these details already exists");
        }
        return user.getStatus();
    }

    public LoginResponse login(LoginRequest req) {
        if (req.email() == null || req.password() == null) throw invalidCredentials();

        User user = userRepository.findByEmail(req.email().trim())
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

    private Driver buildDriver(RegisterRequest.DriverRequest d) {
        if (d == null) throw bad("Driver details are required");
        String nid = clean(d.nationalId());
        String plate = clean(d.plateNumber());
        String lic = clean(d.drivingLicense());
        if (nid.isEmpty() || nid.length() > 30) throw bad("National ID is required (max 30 characters)");
        if (plate.isEmpty() || plate.length() > 20) throw bad("Plate number is required (max 20 characters)");
        if (lic.isEmpty() || lic.length() > 50) throw bad("Driving license is required (max 50 characters)");
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
        if (restaurantName.isEmpty() || restaurantName.length() > 100) throw bad("Restaurant name is required (max 100 characters)");
        if (branch.isEmpty() || branch.length() > 100) throw bad("Branch is required (max 100 characters)");
        RestaurantOwner owner = new RestaurantOwner();
        owner.setRestaurantName(restaurantName);
        owner.setBranch(branch);
        return owner;
    }

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
}