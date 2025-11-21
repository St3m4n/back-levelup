package com.levelup.backend.dto.levelup;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LevelUpReferralResponse {
    private final boolean ok;
    private final String reason;
    private final Integer newUserPoints;
    private final Integer referrerPoints;
    private final String refRun;
}
