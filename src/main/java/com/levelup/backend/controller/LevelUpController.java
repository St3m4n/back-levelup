package com.levelup.backend.controller;

import com.levelup.backend.dto.levelup.LevelUpReferralRequest;
import com.levelup.backend.dto.levelup.LevelUpReferralResponse;
import com.levelup.backend.dto.levelup.LevelUpStatsDto;
import com.levelup.backend.dto.levelup.PurchasePointsRequest;
import com.levelup.backend.dto.levelup.PurchasePointsResponse;
import com.levelup.backend.service.LevelUpStatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/levelup")
@RequiredArgsConstructor
public class LevelUpController {
    private final LevelUpStatsService levelUpStatsService;

    @GetMapping("/{run}/stats")
    public ResponseEntity<LevelUpStatsDto> stats(@PathVariable String run) {
        LevelUpStatsDto stats = levelUpStatsService.getStats(run);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/referrals")
    public ResponseEntity<LevelUpReferralResponse> referrals(@Valid @RequestBody LevelUpReferralRequest request) {
        LevelUpReferralResponse response = levelUpStatsService.applyReferral(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/purchases")
    public ResponseEntity<PurchasePointsResponse> purchases(@Valid @RequestBody PurchasePointsRequest request) {
        PurchasePointsResponse response = levelUpStatsService.addPurchasePoints(request);
        return ResponseEntity.ok(response);
    }
}
