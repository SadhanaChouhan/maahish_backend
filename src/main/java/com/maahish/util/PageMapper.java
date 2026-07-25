package com.maahish.util;

import com.maahish.dto.response.PageResponse;
import org.springframework.data.domain.Page;

import java.util.function.Function;
import java.util.stream.Collectors;

public final class PageMapper {

    private PageMapper() {}

    public static <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        return PageResponse.<R>builder()
                .content(page.getContent().stream().map(mapper).collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
