package com.loqma.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loqma.auth.controller.AuthController;
import com.loqma.auth.dto.LoginRequest;
import com.loqma.auth.dto.LoginResponse;
import com.loqma.auth.dto.RegisterRequest;
import com.loqma.auth.service.AuthService;
import com.loqma.common.enums.AccountStatus;
import com.loqma.common.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    void registerCustomerReturns201() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "Omar Aly", "omar@example.com", "01000000001", "StrongPass#123",
                UserRole.CUSTOMER,
                List.of(new RegisterRequest.AddressRequest("Home", "Tahrir St", "Bldg 1")),
                null, null);
        when(authService.register(any(RegisterRequest.class))).thenReturn(AccountStatus.ACTIVE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void registerOwnerMultipartReturns202() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "Owner Name", "owner@example.com", "01000000002", "StrongPass#123",
                UserRole.RESTAURANT_OWNER, List.of(), null,
                new RegisterRequest.RestaurantRequest(
                        "Koshary", null, "Downtown", "Egyptian", "12 Tahrir",
                        null, "30.044400,31.235700", 30.0444, 31.2357,
                        15.0, 50.0, 25.0, 10.0, null, null));

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsString(req).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile logoPart = new MockMultipartFile(
                "logo", "logo.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});
        MockMultipartFile coverPart = new MockMultipartFile(
                "coverImage", "cover.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{4, 5, 6});

        when(authService.register(any(RegisterRequest.class), any(), any()))
                .thenReturn(AccountStatus.PENDING_APPROVAL);

        mockMvc.perform(multipart("/api/auth/register")
                        .file(dataPart)
                        .file(logoPart)
                        .file(coverPart))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));

        verify(authService).register(any(RegisterRequest.class), any(), any());
    }

    @Test
    void loginReturnsTokenAndRole() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("jwt-token", "CUSTOMER"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"omar@example.com\",\"password\":\"StrongPass#123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void meReturnsAuthenticatedUserSummary() throws Exception {
        when(authService.me("user-123")).thenReturn(Map.of(
                "name", "Omar Aly", "email", "omar@example.com",
                "role", "CUSTOMER", "status", "ACTIVE"));

        mockMvc.perform(get("/api/auth/me")
                        .principal(new UsernamePasswordAuthenticationToken("user-123", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("omar@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }
}
