package com.levelup.backend.controller;

import com.levelup.backend.dto.user.UserDto;
import com.levelup.backend.dto.user.UserListResponseDto;
import com.levelup.backend.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserAdminController {
    private final UserAdminService userAdminService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERADMIN')")
    public ResponseEntity<UserListResponseDto> list(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int limit) {
        Page<UserDto> users = userAdminService.listUsers(activo, region, search, sortBy, order, page, limit);
        int safePage = Math.max(page, 1);
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        UserListResponseDto response = UserListResponseDto.builder()
                .data(users.getContent())
                .meta(UserListResponseDto.Meta.builder()
                        .page(safePage)
                        .limit(safeLimit)
                        .total(users.getTotalElements())
                        .build())
                .build();
        return ResponseEntity.ok(response);
    }
}