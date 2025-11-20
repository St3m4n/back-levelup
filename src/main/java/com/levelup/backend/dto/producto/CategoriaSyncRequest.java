package com.levelup.backend.dto.producto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaSyncRequest {
    @NotEmpty
    private List<String> nombres;
}