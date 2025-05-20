package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;

import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.repository.XpRepository;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class XpService {
    @Autowired
    private XpRepository xpRepository;
    
    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(XpService.class);

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
    public String adicionarXp(Long userId, int xpGanho) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        Xp xp = getOrCreateXp(user);
        int oldLevel = xp.getLevel();
        int oldXp = xp.getXpPoints();
        // Limite de nível 100
        if (xp.getLevel() >= 100) {
            return "Você já atingiu o nível máximo (100). Não é possível ganhar mais XP.";
        }
        xp.addXp(xpGanho);
        if (xp.getLevel() > 100) {
            xp.setLevel(100);
            xp.setXpPoints(0);
        }
        xpRepository.save(xp);
        if (xp.getLevel() > oldLevel) {
            return "Parabéns! Você subiu para o nível " + xp.getLevel() + "! Volte para a Zona de Segurança para escolher seu novo item.";
        }
        return "XP adicionado com sucesso. XP atual: " + xp.getXpPoints() + ", Nível atual: " + xp.getLevel();
    }

    // Obtém a XP atual do jogador
    public Xp obterXp(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
                
        return getOrCreateXp(user);
    }
    
    // Obtém o nível atual do jogador
    public int obterNivel(Long userId) {
        return obterXp(userId).getLevel();
    }
    
    // Obtém os pontos de XP atual do jogador
    public int obterPontosXp(Long userId) {
        return obterXp(userId).getXpPoints();
    }

    public Page<Xp> listAll(Pageable pageable) {
        logger.info("Listing all XP records with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return xpRepository.findAll(pageable);
    }
}