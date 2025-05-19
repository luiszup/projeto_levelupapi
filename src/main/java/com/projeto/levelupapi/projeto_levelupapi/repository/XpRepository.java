package com.projeto.levelupapi.projeto_levelupapi.repository;

import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface XpRepository extends JpaRepository<Xp, Long> {
    Optional<Xp> findByUserId(Long userId);  // Buscar XP do jogador pelo ID do usuário
}
