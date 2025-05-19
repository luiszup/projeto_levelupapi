package com.projeto.levelupapi.dto;

import lombok.Data;

@Data
public class AuthResponseDto {
    private String token;
    private String tokenType = "Bearer";

    private Long userId;
    private String username;
    private int level;
    private int xp;
}
