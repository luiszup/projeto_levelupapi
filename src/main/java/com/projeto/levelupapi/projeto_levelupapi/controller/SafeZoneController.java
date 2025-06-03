package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.service.UserService;
import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/safezone")
@Tag(name = "🛡️ Zona de Segurança", description = "Controle de entrada e saída da Zona de Segurança - necessária para escolher itens de level up")
@SecurityRequirement(name = "bearerAuth")
public class SafeZoneController {
    private final UserService userService;

    public SafeZoneController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{userId}/enter")
    @Operation(
        summary = "Entrar na Zona de Segurança",
        description = "Coloca o jogador na Zona de Segurança. IMPORTANTE: Apenas jogadores na Zona de Segurança podem escolher itens de recompensa ao subir de nível."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jogador entrou na Zona de Segurança com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<String> enterSafeZone(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setInSafeZone(true);
        userService.update(user.getId(), user);
        return ResponseEntity.ok("Você entrou na Zona de Segurança.");
    }

    @PostMapping("/{userId}/exit")
    @Operation(
        summary = "Sair da Zona de Segurança",
        description = "Remove o jogador da Zona de Segurança. ATENÇÃO: Jogadores fora da Zona de Segurança não podem escolher itens de recompensa."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jogador saiu da Zona de Segurança com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<String> exitSafeZone(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setInSafeZone(false);
        userService.update(user.getId(), user);
        return ResponseEntity.ok("Você saiu da Zona de Segurança.");
    }

    @GetMapping("/{userId}/status")
    @Operation(
        summary = "Verificar status da Zona de Segurança",
        description = "Retorna true se o jogador está na Zona de Segurança, false caso contrário"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<Boolean> getSafeZoneStatus(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(user.isInSafeZone());
    }
}
