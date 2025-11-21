package com.levelup.backend.dto.user;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserListResponseDto {
    private final List<UserDto> data;
    private final Meta meta;

    @Getter
    @Builder
    public static class Meta {
        private final int page;
        private final int limit;
        private final long total;
    }
}