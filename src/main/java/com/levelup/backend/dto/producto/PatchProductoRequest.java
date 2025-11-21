package com.levelup.backend.dto.producto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchProductoRequest {
    @NotNull
    private Boolean eliminado;
}
