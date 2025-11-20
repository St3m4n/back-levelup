package com.levelup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserAddressRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    private String line1;

    private String city;

    private String region;

    private String country;

    private Boolean isPrimary;
}
