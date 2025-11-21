package com.levelup.backend.dto.levelup;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LevelUpReferralDto {
    private final String email;
    private final LocalDateTime date;
}
