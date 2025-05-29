package com.projeto.levelupapi.projeto_levelupapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MissionRequestDto {
    
    @NotBlank(message = "Nome da missão é obrigatório")
    private String name;
    
    private String description;
    
    @NotNull(message = "Recompensa de XP é obrigatória")
    @Min(value = 1, message = "Recompensa de XP deve ser pelo menos 1")
    private Integer xpReward;
    
    @NotNull(message = "Nível necessário é obrigatório")
    @Min(value = 1, message = "Nível necessário deve ser pelo menos 1")
    private Integer requiredLevel;
    
    private Boolean isRepeatable = false;
}
