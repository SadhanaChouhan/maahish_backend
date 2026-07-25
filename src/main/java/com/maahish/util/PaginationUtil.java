package com.maahish.util;

import com.maahish.constants.AppConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtil {

    private PaginationUtil() {}

    public static Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String field = sortBy != null && !sortBy.isBlank() ? sortBy : AppConstants.DEFAULT_SORT_BY;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize, Sort.by(direction, field));
    }
}
