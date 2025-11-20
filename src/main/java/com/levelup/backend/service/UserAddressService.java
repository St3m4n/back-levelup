package com.levelup.backend.service;

import com.levelup.backend.dto.UserAddressDto;
import com.levelup.backend.dto.CreateUserAddressRequest;
import com.levelup.backend.dto.UpdateUserAddressRequest;
import com.levelup.backend.model.UserAddress;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.model.UsuarioPerfil;
import com.levelup.backend.repository.UserAddressRepository;
import com.levelup.backend.repository.UsuarioRepository;
import com.levelup.backend.security.LevelUpUserDetails;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserAddressService {
    private final UserAddressRepository addressRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<UserAddressDto> list(String run) {
        Usuario usuario = loadUsuarioAndAuthorize(run);
        return addressRepository.findByUsuarioOrderByPrimaryAddressDescCreatedAtDesc(usuario)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserAddressDto create(String run, CreateUserAddressRequest request) {
        Usuario usuario = loadUsuarioAndAuthorize(run);
        List<UserAddress> existing = addressRepository.findByUsuario(usuario);
        boolean availability = Boolean.TRUE.equals(request.getIsPrimary()) || existing.isEmpty();
        UserAddress address = UserAddress.builder()
                .id(UUID.randomUUID().toString())
                .usuario(usuario)
                .fullName(request.getFullName().trim())
                .line1(request.getLine1().trim())
                .city(trimToNull(request.getCity()))
                .region(trimToNull(request.getRegion()))
                .country(nvlCountry(request.getCountry()))
                .primaryAddress(availability)
                .build();
        UserAddress saved = addressRepository.save(address);
        if (saved.isPrimaryAddress()) {
            ensureSinglePrimary(saved, usuario);
        }
        return toDto(saved);
    }

    @Transactional
    public UserAddressDto update(String run, String addressId, UpdateUserAddressRequest request) {
        Usuario usuario = loadUsuarioAndAuthorize(run);
        UserAddress address = addressRepository.findByIdAndUsuarioRun(addressId, run)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada"));
        address.setFullName(request.getFullName().trim());
        address.setLine1(request.getLine1().trim());
        address.setCity(trimToNull(request.getCity()));
        address.setRegion(trimToNull(request.getRegion()));
        address.setCountry(nvlCountry(request.getCountry()));
        if (request.getIsPrimary() != null) {
            address.setPrimaryAddress(request.getIsPrimary());
        }
        if (address.isPrimaryAddress()) {
            ensureSinglePrimary(address, usuario);
        }
        return toDto(address);
    }

    @Transactional
    public List<UserAddressDto> delete(String run, String addressId) {
        Usuario usuario = loadUsuarioAndAuthorize(run);
        UserAddress address = addressRepository.findByIdAndUsuarioRun(addressId, run)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada"));
        boolean wasPrimary = address.isPrimaryAddress();
        addressRepository.delete(address);
        if (wasPrimary) {
            addressRepository.findByUsuarioOrderByPrimaryAddressDescCreatedAtDesc(usuario)
                    .stream()
                    .findFirst()
                    .ifPresent(next -> {
                        if (!next.isPrimaryAddress()) {
                            next.setPrimaryAddress(true);
                        }
                    });
        }
        return list(run);
    }

    @Transactional
    public UserAddressDto promote(String run, String addressId) {
        Usuario usuario = loadUsuarioAndAuthorize(run);
        UserAddress address = addressRepository.findByIdAndUsuarioRun(addressId, run)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada"));
        if (!address.isPrimaryAddress()) {
            address.setPrimaryAddress(true);
            ensureSinglePrimary(address, usuario);
        }
        return toDto(address);
    }

    private void ensureSinglePrimary(UserAddress target, Usuario usuario) {
        if (!target.isPrimaryAddress()) {
            return;
        }
        addressRepository.findByUsuario(usuario)
                .stream()
                .filter(address -> !address.getId().equals(target.getId()))
                .filter(UserAddress::isPrimaryAddress)
                .forEach(address -> address.setPrimaryAddress(false));
    }

    private Usuario loadUsuarioAndAuthorize(String run) {
        Usuario usuario = usuarioRepository.findByRun(run)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + run));
        authorize(run);
        return usuario;
    }

    private void authorize(String run) {
        LevelUpUserDetails principal = getPrincipal();
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        String actorRun = principal.getUser().getRun();
        UsuarioPerfil perfil = principal.getUser().getPerfil();
        boolean isAdmin = perfil == UsuarioPerfil.Administrador || perfil == UsuarioPerfil.Vendedor;
        if (!isAdmin && !actorRun.equals(run)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
    }

    private LevelUpUserDetails getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof LevelUpUserDetails details ? details : null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String nvlCountry(String country) {
        String sanitized = trimToNull(country);
        return sanitized == null ? "Chile" : sanitized;
    }

    private UserAddressDto toDto(UserAddress address) {
        return UserAddressDto.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .line1(address.getLine1())
                .city(address.getCity())
                .region(address.getRegion())
                .country(address.getCountry())
                .isPrimary(address.isPrimaryAddress())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
