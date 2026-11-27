package com.loqma.admin;

import com.loqma.admin.controller.AdminController;
import com.loqma.auth.repository.UserRepository;
import com.loqma.common.enums.AccountStatus;
import com.loqma.common.enums.UserRole;
import com.loqma.driver.entity.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(userRepository)).build();
    }

    @Test
    void pendingListsPendingUsers() throws Exception {
        Driver driver = new Driver();
        driver.setId("d1");
        driver.setName("Ali");
        driver.setEmail("ali@example.com");
        driver.setRole(UserRole.DRIVER);
        driver.setStatus(AccountStatus.PENDING_APPROVAL);

        when(userRepository.findByStatus(AccountStatus.PENDING_APPROVAL)).thenReturn(List.of(driver));

        mockMvc.perform(get("/api/admin/users/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("d1"))
                .andExpect(jsonPath("$[0].role").value("DRIVER"));
    }

    @Test
    void approveSetsActiveStatus() throws Exception {
        Driver driver = new Driver();
        driver.setId("d1");
        driver.setStatus(AccountStatus.PENDING_APPROVAL);
        driver.setRejectionReason("old");

        when(userRepository.findById("d1")).thenReturn(Optional.of(driver));

        mockMvc.perform(patch("/api/admin/users/d1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Approved"));

        assertEquals(AccountStatus.ACTIVE, driver.getStatus());
        assertNull(driver.getRejectionReason());
        verify(userRepository).save(driver);
    }

    @Test
    void rejectHandlesMissingRequestBodyWithoutNpe() throws Exception {
        Driver driver = new Driver();
        driver.setId("d1");
        driver.setStatus(AccountStatus.PENDING_APPROVAL);

        when(userRepository.findById("d1")).thenReturn(Optional.of(driver));

        mockMvc.perform(patch("/api/admin/users/d1/reject")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rejected"));

        assertEquals(AccountStatus.REJECTED, driver.getStatus());
        assertNull(driver.getRejectionReason());
        verify(userRepository).save(driver);
    }
}
