package com.levelup.backend.service;

import com.levelup.backend.dto.producto.CreateProductoRequest;
import com.levelup.backend.dto.producto.PatchProductoRequest;
import com.levelup.backend.dto.producto.ProductoDto;
import com.levelup.backend.dto.producto.UpdateProductoRequest;
import com.levelup.backend.model.Categoria;
import com.levelup.backend.model.Producto;
import com.levelup.backend.repository.ProductoRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@SuppressWarnings("nullness")
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;

    @Transactional(readOnly = true)
    public List<ProductoDto> list(Boolean includeDeleted, String category, String query) {
        Specification<Producto> spec = safeCombine(null, includeDeletedSpec(includeDeleted));
        spec = safeCombine(spec, categorySpec(category));
        spec = safeCombine(spec, searchSpec(query));
        List<Producto> productos = spec == null
                ? productoRepository.findAll(Sort.by("nombre"))
                : productoRepository.findAll(spec, Sort.by("nombre"));
        return productos.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoDto getProduct(String codigo, boolean includeDeleted) {
        Producto producto = findByCodigoOrThrow(codigo);
        if (!includeDeleted && producto.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + producto.getCodigo());
        }
        return toDto(producto);
    }

    public ProductoDto getProduct(String codigo) {
        return getProduct(codigo, false);
    }

    @Transactional
    @SuppressWarnings("nullness")
    public ProductoDto createProduct(CreateProductoRequest request) {
        String normalizedCodigo = normalizeCodigo(request.getCodigo());
        String codigo = normalizedCodigo == null || normalizedCodigo.isBlank()
                ? generateUniqueCodigo()
                : normalizedCodigo;
        if (productoRepository.existsByCodigo(codigo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con el código " + codigo);
        }
        Categoria categoria = categoriaService.ensureCategory(request.getCategoria());
        Producto producto = Producto.builder()
                .codigo(codigo)
                .nombre(request.getNombre().trim())
                .descripcion(trimToNull(request.getDescripcion()))
                .categoria(categoria)
                .fabricante(trimToNull(request.getFabricante()))
                .distribuidor(trimToNull(request.getDistribuidor()))
                .precio(request.getPrecio())
                .stock(request.getStock())
                .stockCritico(request.getStockCritico())
                .imagenUrl(trimToNull(request.getImagenUrl()))
                .build();
        try {
            productoRepository.save(producto);
            return toDto(producto);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se pudo crear el producto con el código " + codigo);
        }
    }

    @Transactional
    @SuppressWarnings("nullness")
    public ProductoDto updateProduct(String codigo, UpdateProductoRequest request) {
        Producto producto = findByCodigoOrThrow(codigo);
        producto.setNombre(request.getNombre().trim());
        producto.setDescripcion(trimToNull(request.getDescripcion()));
        producto.setCategoria(categoriaService.ensureCategory(request.getCategoria()));
        producto.setFabricante(trimToNull(request.getFabricante()));
        producto.setDistribuidor(trimToNull(request.getDistribuidor()));
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setStockCritico(request.getStockCritico());
        producto.setImagenUrl(trimToNull(request.getImagenUrl()));
        productoRepository.save(producto);
        return toDto(producto);
    }

    @Transactional
    @SuppressWarnings("nullness")
    public ProductoDto softDeleteProduct(String codigo) {
        Producto producto = findByCodigoOrThrow(codigo);
        if (producto.getDeletedAt() == null) {
            producto.setDeletedAt(LocalDateTime.now());
        }
        productoRepository.save(producto);
        return toDto(producto);
    }

    @Transactional
    @SuppressWarnings("nullness")
    public ProductoDto restoreProduct(String codigo) {
        Producto producto = findByCodigoOrThrow(codigo);
        if (producto.getDeletedAt() != null) {
            producto.setDeletedAt(null);
        }
        productoRepository.save(producto);
        return toDto(producto);
    }

    @Transactional
    @SuppressWarnings("nullness")
    public ProductoDto updateDeletionStatus(String codigo, PatchProductoRequest request) {
        Producto producto = findByCodigoOrThrow(codigo);
        if (Boolean.TRUE.equals(request.getEliminado())) {
            if (producto.getDeletedAt() == null) {
                producto.setDeletedAt(LocalDateTime.now());
            }
        } else if (Boolean.FALSE.equals(request.getEliminado())) {
            producto.setDeletedAt(null);
        }
        productoRepository.save(producto);
        return toDto(producto);
    }

    private Producto findByCodigoOrThrow(String codigo) {
        String normalized = normalizeCodigo(codigo);
        if (normalized == null || normalized.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido");
        }
        return productoRepository.findByCodigo(normalized)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado: " + normalized));
    }

    private Specification<Producto> includeDeletedSpec(Boolean includeDeleted) {
        if (Boolean.TRUE.equals(includeDeleted)) {
            return null;
        }
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    private Specification<Producto> categorySpec(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return null;
        }
        String normalized = categoryName.trim().toLowerCase(Locale.ROOT);
        return (root, query, builder) -> {
            Join<Producto, Categoria> join = root.join("categoria");
            return builder.equal(builder.lower(join.get("nombre")), normalized);
        };
    }

    private Specification<Producto> searchSpec(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, builder) -> {
            Expression<String> nombre = builder.lower(root.get("nombre"));
            Expression<String> descripcion = builder.lower(builder.coalesce(root.get("descripcion"), ""));
            Expression<String> codigo = builder.lower(root.get("codigo"));
            Predicate nombreMatch = builder.like(nombre, pattern);
            Predicate descripcionMatch = builder.like(descripcion, pattern);
            Predicate codigoMatch = builder.like(codigo, pattern);
            return builder.or(nombreMatch, descripcionMatch, codigoMatch);
        };
    }

    private Specification<Producto> safeCombine(Specification<Producto> base, Specification<Producto> addition) {
        if (base == null) {
            return addition;
        }
        return addition == null ? base : base.and(addition);
    }

    private String normalizeCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private String generateUniqueCodigo() {
        String codigo;
        do {
            codigo = "SKU-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        } while (productoRepository.existsByCodigo(codigo));
        return codigo;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProductoDto toDto(Producto producto) {
        return ProductoDto.builder()
                .codigo(producto.getCodigo())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .categoria(producto.getCategoria().getNombre())
                .fabricante(producto.getFabricante())
                .distribuidor(producto.getDistribuidor())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .stockCritico(producto.getStockCritico())
                .imagenUrl(producto.getImagenUrl())
                .eliminado(producto.getDeletedAt() != null)
                .deletedAt(producto.getDeletedAt())
                .createdAt(producto.getCreatedAt())
                .updatedAt(producto.getUpdatedAt())
                .build();
    }
}
