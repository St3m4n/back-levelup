package com.levelup.backend.dto.producto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncCategoriasRequest {
    @NotNull
    @NotEmpty
    private List<@NotBlank String> nombres;
}
