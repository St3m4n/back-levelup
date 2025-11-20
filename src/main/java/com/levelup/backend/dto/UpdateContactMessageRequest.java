package com.levelup.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateContactMessageRequest {
    private String status;

    private String respuesta;
}
