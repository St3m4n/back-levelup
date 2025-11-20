package com.levelup.backend.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserAddressDto {
    private final String id;
    private final String fullName;
    private final String line1;
    private final String city;
    private final String region;
    private final String country;
    private final boolean isPrimary;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
