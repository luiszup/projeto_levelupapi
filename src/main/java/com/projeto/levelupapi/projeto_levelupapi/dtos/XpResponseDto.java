package com.projeto.levelupapi.dto;

import lombok.Data;

@Data
public class XpResponseDto {
    private Long userId;
    private int xpPoints;
    private int level;
}