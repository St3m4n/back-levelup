package com.levelup.backend.dto.levelup;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LevelUpExpDto {
    private final int compras;
    private final int torneos;
    private final int referidos;
}
