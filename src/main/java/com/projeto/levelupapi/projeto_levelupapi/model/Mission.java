package com.projeto.levelupapi.projeto_levelupapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "missions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer xpReward;
    
    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer requiredLevel;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private Boolean isRepeatable = false;
}
