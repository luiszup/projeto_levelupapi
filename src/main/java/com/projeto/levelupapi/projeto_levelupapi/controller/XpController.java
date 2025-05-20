package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.service.XpService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/xp")
public class XpController {

    @Autowired
    private XpService xpService;

    // Endpoint para adicionar XP ao jogador
    @PostMapping("/{userId}/add")
    public ResponseEntity<String> adicionarXp(@PathVariable Long userId, @RequestParam int xpGanho) {
        String mensagem = xpService.adicionarXp(userId, xpGanho);
        return ResponseEntity.ok(mensagem);
    }

    // Endpoint para obter a XP do jogador
    @GetMapping("/{userId}")
    public ResponseEntity<Integer> obterXp(@PathVariable Long userId) {
        return ResponseEntity.ok(xpService.obterXp(userId).getXpPoints());
    }
}
