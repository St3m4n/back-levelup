package com.levelup.backend.repository;

import com.levelup.backend.model.UserAddress;
import com.levelup.backend.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress, String> {
    List<UserAddress> findByUsuarioRunOrderByPrimaryAddressDescCreatedAtDesc(String run);

    List<UserAddress> findByUsuarioOrderByPrimaryAddressDescCreatedAtDesc(Usuario usuario);

    List<UserAddress> findByUsuario(Usuario usuario);

    Optional<UserAddress> findByIdAndUsuarioRun(String id, String run);
}