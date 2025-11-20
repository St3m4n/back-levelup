package com.levelup.backend.repository;

import com.levelup.backend.model.Producto;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {
    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}