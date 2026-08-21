package com.gamifiedstudyhub.backend.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for recording a study activity. */
public record ActivityRequest(
        @NotBlank @Size(max = 64) String type,
        @NotBlank @Size(max = 500) String message
) {
}
