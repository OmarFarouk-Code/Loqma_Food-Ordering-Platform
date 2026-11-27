package com.loqma.customer.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewDto {
    private String id;
    private String customerName; 
    private Integer rating;     
    private String comment;
    private LocalDateTime createdAt;
}