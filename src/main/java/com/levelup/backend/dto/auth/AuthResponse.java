package com.levelup.backend.dto.auth;

import com.levelup.backend.dto.UserProfileDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private final String token;
    private final String tokenType;
    private final UserProfileDto user;
}
