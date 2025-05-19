
package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.repository.XpRepository;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class XpService {

    @Autowired
    private XpRepository xpRepository;

    @Autowired
    private UserRepository userRepository;

    // Adiciona XP ao jogador
    public void adicionarXp(Long userId, int xpGanho) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Xp xp = xpRepository.findByUserId(userId)
                .orElse(new Xp(null, user, 0, 1));  // Cria um novo registro de XP se não existir ainda

        xp.addXp(xpGanho);  // Adiciona o XP ao jogador

        xpRepository.save(xp);  // Persiste o novo XP
    }

    // Obtém a XP atual do jogador
    public Xp obterXp(Long userId) {
        return xpRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("XP não encontrado para o usuário"));
    }
}
