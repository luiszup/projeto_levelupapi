package com.projeto.levelupapi.projeto_levelupapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionHistoryDto {
    private Long id;
    private String missionName;
    private String missionDescription;
    private Integer xpGained;
    private LocalDateTime completedAt;
    private String username;
}
