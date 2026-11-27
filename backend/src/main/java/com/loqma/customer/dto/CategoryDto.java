package com.loqma.customer.dto;
import lombok.*;
import java.util.*;

@Getter
@Builder
public class CategoryDto {
    private String name;
    private List<ItemDto> items;
}