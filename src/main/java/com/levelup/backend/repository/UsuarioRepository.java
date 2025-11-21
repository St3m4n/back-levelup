package com.levelup.backend.repository;

import com.levelup.backend.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByRun(String run);
    boolean existsByCorreo(String correo);
    boolean existsByRun(String run);
}
