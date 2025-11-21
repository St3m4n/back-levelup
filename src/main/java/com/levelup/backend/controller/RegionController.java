package com.levelup.backend.controller;

import com.levelup.backend.dto.location.RegionDto;
import com.levelup.backend.service.RegionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/regiones")
@RequiredArgsConstructor
public class RegionController {
    private final RegionService regionService;

    @GetMapping
    public ResponseEntity<List<RegionDto>> list() {
        return ResponseEntity.ok(regionService.listRegions());
    }
}