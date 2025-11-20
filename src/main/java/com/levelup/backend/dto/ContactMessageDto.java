package com.levelup.backend.dto;

import com.levelup.backend.model.ContactMessageStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContactMessageDto {
    private final String id;
    private final String nombre;
    private final String email;
    private final String asunto;
    private final String mensaje;
    private final ContactMessageStatus status;
    private final String respuesta;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
