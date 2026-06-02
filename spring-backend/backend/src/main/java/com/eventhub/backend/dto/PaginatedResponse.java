package com.eventhub.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PaginatedResponse<T> {
    private List<T> data;
    private int totalPages;
    private long totalItems;
    private int currentPage;
    private int limit;
}
