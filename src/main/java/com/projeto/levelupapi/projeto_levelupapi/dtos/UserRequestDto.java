package com.projeto.levelupapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequestDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}