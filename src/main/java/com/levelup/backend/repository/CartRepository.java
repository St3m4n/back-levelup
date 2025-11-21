package com.levelup.backend.repository;

import com.levelup.backend.model.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUsuarioRun(String run);
}
