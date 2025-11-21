package com.levelup.backend.service;

import com.levelup.backend.dto.location.RegionDto;
import com.levelup.backend.model.Comuna;
import com.levelup.backend.model.Region;
import com.levelup.backend.repository.RegionRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegionService {
    private final RegionRepository regionRepository;

    @Transactional(readOnly = true)
    public List<RegionDto> listRegions() {
        return regionRepository.findAllByOrderByIdAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private RegionDto toDto(Region region) {
        List<String> comunas = region.getComunas().stream()
                .map(Comuna::getNombre)
                .collect(Collectors.toList());
        return RegionDto.builder()
                .id(region.getId())
                .nombre(region.getNombre())
                .comunas(comunas)
                .build();
    }
}