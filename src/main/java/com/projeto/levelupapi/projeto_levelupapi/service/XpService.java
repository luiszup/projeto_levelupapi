package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;

import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.repository.XpRepository;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class XpService {
    @Autowired
    private XpRepository xpRepository;
    
    @Autowired
    private UserRepository userRepository;

    // Método utilitário para obter ou criar XP para um usuário
    private Xp getOrCreateXp(User user) {
        return xpRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Xp newXp = new Xp();
                    newXp.setUser(user);
                    newXp.setXpPoints(0);
                    newXp.setLevel(1);
                    return xpRepository.save(newXp);
                });
    }

    // Adiciona XP ao jogador
    @Transactional
    public void addXp(Long userId, int xpGanho) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
                
        Xp xp = getOrCreateXp(user);
        xp.addXp(xpGanho);
        xpRepository.save(xp);
    }

    // Obtém a XP atual do jogador
    public Xp getXp(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
                
        return getOrCreateXp(user);
    }
    
    // Obtém o nível atual do jogador
    public int getLevel(Long userId) {
        return getXp(userId).getLevel();
    }
    
    // Obtém os pontos de XP atual do jogador
    public int getXpPoints(Long userId) {
        return getXp(userId).getXpPoints();
    }
}