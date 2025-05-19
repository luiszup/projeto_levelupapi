package com.projeto.levelupapi.projeto_levelupapi.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "xp")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Xp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Relacionamento com a entidade User

    private int xpPoints;  // Quantidade de XP do jogador
    private int level;     // Nível do jogador

    // Método para adicionar XP ao jogador
    public void addXp(int xpGanho) {
        this.xpPoints += xpGanho;
        checkLevelUp();
    }

    // Verifica se o jogador subiu de nível
    private void checkLevelUp() {
        while (this.xpPoints >= xpRequiredForLevelUp()) {
            this.level++;
            this.xpPoints -= xpRequiredForLevelUp();  // Reseta a XP extra após subir de nível
        }
    }

    // Calcula a XP necessária para o próximo nível
    private int xpRequiredForLevelUp() {
        return 100 * this.level;  // Exemplo: cada nível exige 100 XP
    }
}
