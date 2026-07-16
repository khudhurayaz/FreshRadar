package de.khudhurayaz.freshradar.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public class Util {
    public static <T> Page<T> getPages(int page, int size, List<T> list) {
        int start = page * size;
        int end = Math.min(start + size, list.size());

        List<T> pageContent = start >= list.size()
                ? List.of()
                : list.subList(start, end);

        return new PageImpl<>(
                pageContent,
                PageRequest.of(page, size),
                list.size()
        );
    }
}
