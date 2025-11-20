package com.levelup.backend.controller;

import com.levelup.backend.dto.producto.CategoriaDto;
import com.levelup.backend.dto.producto.SyncCategoriasRequest;
import com.levelup.backend.service.CategoriaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoriaController {
    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaDto>> list(@RequestParam(defaultValue = "false") boolean includeDeleted) {
        return ResponseEntity.ok(categoriaService.list(includeDeleted));
    }

    @PutMapping
    public ResponseEntity<List<CategoriaDto>> sync(@Valid @RequestBody SyncCategoriasRequest request) {
        return ResponseEntity.ok(categoriaService.sync(request.getNombres()));
    }
}
