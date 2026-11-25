package com.community.common.api;

import java.util.List;

/**
 * 统一分页结构：{@code {items, page, pageSize, total, hasMore}}。
 *
 * <p>契约见 docs/design-proposal-v0.1.md 第 5.2 节。</p>
 */
public record PageResult<T>(List<T> items, int page, int pageSize, long total, boolean hasMore) {

    public static <T> PageResult<T> of(List<T> items, int page, int pageSize, long total) {
        boolean hasMore = (long) page * pageSize < total;
        return new PageResult<>(items, page, pageSize, total, hasMore);
    }
}
