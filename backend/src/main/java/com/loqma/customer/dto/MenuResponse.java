package com.loqma.customer.dto;

import lombok.*;
import java.util.*;

@Getter
@Builder
public class MenuResponse {
    private List<CategoryDto> categories;
}