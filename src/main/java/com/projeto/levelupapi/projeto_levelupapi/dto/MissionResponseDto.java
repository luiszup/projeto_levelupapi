package com.projeto.levelupapi.projeto_levelupapi.dto;

import lombok.Data;

@Data
public class MissionResponseDto {
    private Long id;
    private String name;
    private String description;
    private Integer xpReward;
    private Integer requiredLevel;
    private Boolean isRepeatable;
    private Boolean canComplete; // Se o usuário pode completar esta missão
}
