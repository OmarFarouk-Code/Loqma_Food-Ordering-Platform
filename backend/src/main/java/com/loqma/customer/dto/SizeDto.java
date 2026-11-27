package com.loqma.customer.dto;
import lombok.*;

@Getter
@Builder
public class SizeDto {
    private String id;
    private String name;
    private Double price;
}
