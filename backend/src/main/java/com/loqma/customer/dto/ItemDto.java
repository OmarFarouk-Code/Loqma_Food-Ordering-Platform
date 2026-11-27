package com.loqma.customer.dto;
import lombok.*;
import java.util.*;

@Getter
@Builder
public class ItemDto {
    private String id;
    private String name;
    private String description;
    private Double price;
    private String photo;
    private Boolean available;
    private List<SizeDto> sizes;
    private List<ExtraDto> extras;
}
