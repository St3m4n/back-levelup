package com.levelup.backend.controller;

import com.levelup.backend.dto.producto.CreateProductoRequest;
import com.levelup.backend.dto.producto.ProductoDto;
import com.levelup.backend.dto.producto.PatchProductoRequest;
import com.levelup.backend.dto.producto.UpdateProductoRequest;
import com.levelup.backend.service.ProductoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoDto>> list(@RequestParam(defaultValue = "false") boolean includeDeleted,
                                                  @RequestParam(required = false) String category,
                                                  @RequestParam(required = false) String query) {
        return ResponseEntity.ok(productoService.list(includeDeleted, category, query));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<ProductoDto> getOne(@PathVariable String codigo,
                                              @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return ResponseEntity.ok(productoService.getProduct(codigo, includeDeleted));
    }

    @PostMapping
    public ResponseEntity<ProductoDto> create(@Valid @RequestBody CreateProductoRequest request) {
        ProductoDto creado = productoService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<ProductoDto> update(@PathVariable String codigo, @Valid @RequestBody UpdateProductoRequest request) {
        return ResponseEntity.ok(productoService.updateProduct(codigo, request));
    }

    @PostMapping("/{codigo}/soft-delete")
    public ResponseEntity<ProductoDto> softDelete(@PathVariable String codigo) {
        return ResponseEntity.ok(productoService.softDeleteProduct(codigo));
    }

    @PostMapping("/{codigo}/restore")
    public ResponseEntity<ProductoDto> restore(@PathVariable String codigo) {
        return ResponseEntity.ok(productoService.restoreProduct(codigo));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<ProductoDto> delete(@PathVariable String codigo) {
        return ResponseEntity.ok(productoService.softDeleteProduct(codigo));
    }

    @PatchMapping("/{codigo}")
    public ResponseEntity<ProductoDto> patchDeletionStatus(@PathVariable String codigo,
                                                           @Valid @RequestBody PatchProductoRequest request) {
        return ResponseEntity.ok(productoService.updateDeletionStatus(codigo, request));
    }
}
