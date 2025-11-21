package com.levelup.backend.config;

import com.levelup.backend.model.Comuna;
import com.levelup.backend.model.Region;
import com.levelup.backend.repository.ComunaRepository;
import com.levelup.backend.repository.RegionRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegionSeeder implements CommandLineRunner {
    private final RegionRepository regionRepository;
    private final ComunaRepository comunaRepository;

    private static final List<RegionSeed> REGION_SEED = List.of(
            new RegionSeed("Arica y Parinacota", List.of("Arica", "Putre")),
            new RegionSeed("Tarapacá", List.of("Iquique", "Alto Hospicio", "Pozo Almonte")),
            new RegionSeed("Antofagasta", List.of("Antofagasta", "Calama", "Mejillones", "Tocopilla")),
            new RegionSeed("Atacama", List.of("Copiapó", "Vallenar", "Caldera")),
            new RegionSeed("Coquimbo", List.of("La Serena", "Coquimbo", "Ovalle", "Illapel")),
            new RegionSeed("Valparaíso", List.of("Valparaíso", "Viña del Mar", "Quilpué", "Villa Alemana", "Quillota", "San Antonio")),
            new RegionSeed("Metropolitana de Santiago", List.of("Santiago", "Puente Alto", "Maipú", "Providencia", "Las Condes", "La Florida", "Ñuñoa", "Lo Barnechea", "Recoleta", "Santiago Centro")),
            new RegionSeed("O'Higgins", List.of("Rancagua", "San Fernando", "Santa Cruz")),
            new RegionSeed("Maule", List.of("Talca", "Curicó", "Linares", "Constitución")),
            new RegionSeed("Ñuble", List.of("Chillán", "San Carlos", "Bulnes")),
            new RegionSeed("Biobío", List.of("Concepción", "Talcahuano", "San Pedro de la Paz", "Coronel", "Chiguayante", "Hualpén", "Los Ángeles")),
            new RegionSeed("La Araucanía", List.of("Temuco", "Padre Las Casas", "Villarrica", "Angol")),
            new RegionSeed("Los Ríos", List.of("Valdivia", "La Unión", "Río Bueno")),
            new RegionSeed("Los Lagos", List.of("Puerto Montt", "Osorno", "Castro", "Ancud", "Puerto Varas")),
            new RegionSeed("Aysén", List.of("Coyhaique", "Puerto Aysén", "Chile Chico")),
            new RegionSeed("Magallanes", List.of("Punta Arenas", "Puerto Natales", "Porvenir"))
    );

    @Override
    @Transactional
    public void run(String... args) {
        for (RegionSeed seed : REGION_SEED) {
            Region region = regionRepository.findByNombreIgnoreCase(seed.nombre())
                    .orElseGet(() -> Objects.requireNonNull(saveNewRegion(seed.nombre())));
            for (String comunaName : seed.comunas()) {
                if (!comunaRepository.existsByNombreIgnoreCaseAndRegion(comunaName, region)) {
                    Objects.requireNonNull(saveNewComuna(comunaName, region));
                }
            }
        }
        log.debug("Seeded {} regiones", REGION_SEED.size());
    }

    private Region saveNewRegion(String nombre) {
        Region region = new Region();
        region.setNombre(nombre);
        return regionRepository.save(region);
    }

    private Comuna saveNewComuna(String nombre, Region region) {
        Comuna comuna = new Comuna();
        comuna.setNombre(nombre);
        comuna.setRegion(region);
        return comunaRepository.save(comuna);
    }

    private record RegionSeed(String nombre, List<String> comunas) {
    }
}