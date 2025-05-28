package com.projeto.levelupapi.projeto_levelupapi.dto;

import lombok.Data;

@Data
public class MissionCompletionResponseDto {
    private String message;
    private Integer xpGained;
    private Integer currentXp;
    private Integer currentLevel;
    private Boolean levelUp;
    private String missionName;
    
    public MissionCompletionResponseDto(String message, Integer xpGained, Integer currentXp, 
                                      Integer currentLevel, Boolean levelUp, String missionName) {
        this.message = message;
        this.xpGained = xpGained;
        this.currentXp = currentXp;
        this.currentLevel = currentLevel;
        this.levelUp = levelUp;
        this.missionName = missionName;
    }
}
