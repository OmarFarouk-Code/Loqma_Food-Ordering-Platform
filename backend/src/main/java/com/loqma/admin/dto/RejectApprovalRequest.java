package com.loqma.admin.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectApprovalRequest(
    @NotBlank(message = "Rejection reason is required")
    @Size(max = 255, message = "Rejection reason must be at most 255 characters")
    String reason
) {}

