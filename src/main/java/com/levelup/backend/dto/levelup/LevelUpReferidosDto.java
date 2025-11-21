package com.levelup.backend.dto.levelup;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LevelUpReferidosDto {
    private final int count;
    private final List<LevelUpReferralDto> users;
}
