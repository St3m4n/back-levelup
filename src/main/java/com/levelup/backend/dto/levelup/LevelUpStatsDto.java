package com.levelup.backend.dto.levelup;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LevelUpStatsDto {
    private final String run;
    private final int points;
    private final LevelUpExpDto exp;
    private final String referralCode;
    private final String referredBy;
    private final LevelUpReferidosDto referidos;
    private final LocalDateTime updatedAt;
}
