package com.projeto.levelupapi.dto;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String username;
    private int level;
    private int xp;
}