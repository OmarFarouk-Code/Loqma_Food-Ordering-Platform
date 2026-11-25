package com.loqma.customer.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PagedResponse<T> {
    private List<T> items;
    private int page;          // starts at 0
    private int size;
    private long totalItems;
    private int totalPages;
    private boolean hasNext;
}