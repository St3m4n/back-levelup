package com.levelup.backend.repository;

import com.levelup.backend.model.Region;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByNombreIgnoreCase(String nombre);

    @EntityGraph(attributePaths = "comunas")
    List<Region> findAllByOrderByIdAsc();
}