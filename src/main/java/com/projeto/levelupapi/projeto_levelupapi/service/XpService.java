package com.projeto.levelupapi.service;

import com.projeto.levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.model.Xp;
import com.projeto.levelupapi.repository.XpRepository;
import com.projeto.levelupapi.model.User;
import com.projeto.levelupapi.repository.UserRepository;
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
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado"));
                
        Xp xp = xpRepository.findByUserId(userId)
                .orElse(new Xp(null, user, 0, 1));  // Cria um novo registro de XP se não existir ainda
                
        xp.addXp(xpGanho);  // Adiciona o XP ao jogador
        xpRepository.save(xp);  // Persiste o novo XP
    }

    // Obtém a XP atual do jogador
    public Xp obterXp(Long userId) {
        return xpRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("XP não encontrado para o usuário com ID " + userId));
    }
}