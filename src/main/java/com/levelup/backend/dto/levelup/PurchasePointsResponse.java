package com.levelup.backend.dto.levelup;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchasePointsResponse {
    private final boolean ok;
    private final int pointsAdded;
    private final int totalPoints;
    private final LevelUpStatsDto stats;
}
