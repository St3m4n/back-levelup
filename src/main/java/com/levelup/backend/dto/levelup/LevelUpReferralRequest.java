package com.levelup.backend.dto.levelup;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelUpReferralRequest {
    @NotBlank
    @Size(min = 4)
    private String newRun;

    @NotBlank
    private String referralCode;

    @Email
    private String newEmail;
}
