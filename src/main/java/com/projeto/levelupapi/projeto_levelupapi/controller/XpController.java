package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.service.XpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/xp")
@Tag(name = "⭐ Sistema de XP", description = "Gerenciamento de experiência e progressão de nível dos jogadores")
@SecurityRequirement(name = "bearerAuth")
public class XpController {
    @Autowired
    private XpService xpService;

    @PostMapping("/{userId}/add")
    @Operation(
        summary = "Adicionar XP ao jogador",
        description = "Adiciona pontos de experiência ao jogador. Se o XP for suficiente, o jogador pode subir de nível automaticamente. " +
                     "Retorna mensagem informando se houve level up ou apenas ganho de XP."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "XP adicionado com sucesso - mensagem indica se houve level up"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "400", description = "Valor de XP inválido (deve ser positivo)"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<String> adicionarXp(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId,
        @Parameter(description = "Quantidade de XP a ser adicionada (deve ser positiva)", required = true)
        @RequestParam int xpGanho) {
        String mensagem = xpService.adicionarXp(userId, xpGanho);
        return ResponseEntity.ok(mensagem);
    }

    @GetMapping("/{userId}")
    @Operation(
        summary = "Consultar XP atual do jogador",
        description = "Retorna a quantidade atual de pontos de experiência do jogador"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "XP retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<Integer> obterXp(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId) {
        return ResponseEntity.ok(xpService.obterXp(userId).getXpPoints());
    }

    @PostMapping("/{userId}/reset")
    @Operation(
        summary = "Resetar XP do jogador (Admin/Teste)",
        description = "Reseta o XP do jogador para 0 e nível para 1. Útil para testes."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "XP resetado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente"),
        @ApiResponse(responseCode = "403", description = "Acesso negado - apenas administradores")
    })
    public ResponseEntity<String> resetXp(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId) {
        String mensagem = xpService.resetXp(userId);
        return ResponseEntity.ok(mensagem);
    }
}
