package com.levelup.backend.repository;

import com.levelup.backend.model.Comuna;
import com.levelup.backend.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComunaRepository extends JpaRepository<Comuna, Long> {
    boolean existsByNombreIgnoreCaseAndRegion(String nombre, Region region);
}