package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.service.ItemService;
import com.projeto.levelupapi.projeto_levelupapi.service.InventoryService;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/levelup")
@Tag(name = "🚀 Sistema de Level Up", description = "Gerenciamento de recompensas e itens desbloqueáveis por nível")
@SecurityRequirement(name = "bearerAuth")
public class LevelUpController {
    private final ItemService itemService;
    private final InventoryService inventoryService;
    private final UserService userService;

    public LevelUpController(ItemService itemService, InventoryService inventoryService, UserService userService) {
        this.itemService = itemService;
        this.inventoryService = inventoryService;
        this.userService = userService;
    }

    @GetMapping("/{userId}/available-items")
    @Operation(
        summary = "Listar itens disponíveis para o nível atual",
        description = "Retorna todos os itens que o jogador pode escolher baseado no seu nível atual. " +
                     "Itens de nível superior não aparecem na lista."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de itens disponíveis retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<List<String>> getAvailableItems(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        int level = user.getXpData() != null ? user.getXpData().getLevel() : 1;
        return ResponseEntity.ok(itemService.getAvailableItemsForLevel(level));
    }

    @PostMapping("/{userId}/choose-item")
    @Operation(
        summary = "Escolher item de recompensa",
        description = "Permite ao jogador escolher um item de recompensa ao subir de nível. " +
                     "REGRA IMPORTANTE: O jogador DEVE estar na Zona de Segurança para escolher itens. " +
                     "O item deve estar disponível para o nível atual do jogador."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Item escolhido e adicionado ao inventário com sucesso"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Erro nas regras de negócio",
            content = @Content(
                examples = {
                    @ExampleObject(name = "Fora da Zona de Segurança", value = "Você precisa estar na Zona de Segurança para escolher um item de level up."),
                    @ExampleObject(name = "Item indisponível", value = "Item não disponível para o seu nível"),
                    @ExampleObject(name = "Nome obrigatório", value = "Nome do item é obrigatório")
                }
            )
        ),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<String> chooseItem(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Nome do item a ser escolhido",
            content = @Content(
                examples = @ExampleObject(
                    value = "{ \"itemName\": \"Espada de Ferro\" }"
                )
            )
        )
        @RequestBody Map<String, String> body) {
        String itemName = body.get("itemName");
        if (itemName == null) {
            return ResponseEntity.badRequest().body("Nome do item é obrigatório");
        }
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isInSafeZone()) {
            return ResponseEntity.badRequest().body("Você precisa estar na Zona de Segurança para escolher um item de level up.");
        }
        int level = user.getXpData() != null ? user.getXpData().getLevel() : 1;
        List<String> available = itemService.getAvailableItemsForLevel(level);
        if (!available.contains(itemName)) {
            return ResponseEntity.badRequest().body("Item não disponível para o seu nível");
        }
        inventoryService.addItem(user, itemName, 1);
        return ResponseEntity.ok("Item " + itemName + " adicionado ao inventário!");
    }
}
