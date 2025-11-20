package com.levelup.backend.repository;

import com.levelup.backend.model.Order;
import com.levelup.backend.model.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrderRepository extends JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {
    List<Order> findByUsuario(Usuario usuario);

    List<Order> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<Order> findByUserEmail(String userEmail);
}
