
// ===== 6. InventoryController =====
package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.exception.BadRequestException;
import com.projeto.levelupapi.projeto_levelupapi.model.InventoryItem;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.service.InventoryService;
import com.projeto.levelupapi.projeto_levelupapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
@RequestMapping("/api/inventory")
@Tag(name = "🎒 Inventário", description = "Gerenciamento do inventário de itens dos jogadores")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserService userService;

    @Autowired
    public InventoryController(InventoryService inventoryService, UserService userService) {
        this.inventoryService = inventoryService;
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    @Operation(
        summary = "Visualizar inventário do jogador",
        description = "Retorna todos os itens no inventário do jogador, incluindo quantidades"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventário retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<List<InventoryItem>> getInventory(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado"));
        List<InventoryItem> inventory = inventoryService.getInventory(user);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/{userId}/paged")
    @Operation(
        summary = "Visualizar inventário com paginação",
        description = "Retorna o inventário do jogador de forma paginada para melhor performance"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página do inventário retornada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public Page<InventoryItem> getInventoryPaged(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId,
        @Parameter(description = "Parâmetros de paginação (page, size, sort)")
        Pageable pageable) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado"));
        return inventoryService.listInventory(user, pageable);
    }

    @PostMapping("/{userId}/add")
    @Operation(
        summary = "Adicionar item ao inventário",
        description = "Adiciona um item ao inventário do jogador. Se o item já existe, aumenta a quantidade. " +
                     "Se não existe, cria um novo item no inventário."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item adicionado ao inventário com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos - nome do item obrigatório ou quantidade inválida"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<InventoryItem> addItem(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do item a ser adicionado",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Apenas nome",
                        value = "{ \"itemName\": \"Poção de Vida\" }"
                    ),
                    @ExampleObject(
                        name = "Nome e quantidade",
                        value = "{ \"itemName\": \"Poção de Vida\", \"quantity\": 5 }"
                    )
                }
            )
        )
        @RequestBody Map<String, Object> body) {
        if (!body.containsKey("itemName") || body.get("itemName") == null) {
            throw new BadRequestException("Nome do item é obrigatório");
        }
        String itemName = body.get("itemName").toString();
        int quantity = 1;
        if (body.containsKey("quantity")) {
            try {
                quantity = Integer.parseInt(body.get("quantity").toString());
                if (quantity <= 0) {
                    throw new BadRequestException("Quantidade deve ser maior que zero");
                }
            } catch (NumberFormatException e) {
                throw new BadRequestException("Quantidade inválida");
            }
        }
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado"));
        InventoryItem item = inventoryService.addItem(user, itemName, quantity);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/{userId}/remove")
    @Operation(
        summary = "Remover item do inventário",
        description = "Remove uma quantidade específica de um item do inventário. Se a quantidade removida for igual ou maior que a quantidade total, o item é completamente removido."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item removido do inventário com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos - nome do item obrigatório ou quantidade inválida"),
        @ApiResponse(responseCode = "404", description = "Jogador ou item não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<Void> removeItem(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long userId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do item a ser removido",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Remover 1 unidade",
                        value = "{ \"itemName\": \"Poção de Vida\" }"
                    ),
                    @ExampleObject(
                        name = "Remover quantidade específica",
                        value = "{ \"itemName\": \"Poção de Vida\", \"quantity\": 3 }"
                    )
                }
            )
        )
        @RequestBody Map<String, Object> body) {
        if (!body.containsKey("itemName") || body.get("itemName") == null) {
            throw new BadRequestException("Nome do item é obrigatório");
        }
        String itemName = body.get("itemName").toString();
        int quantity = 1;
        if (body.containsKey("quantity")) {
            try {
                quantity = Integer.parseInt(body.get("quantity").toString());
                if (quantity <= 0) {
                    throw new BadRequestException("Quantidade deve ser maior que zero");
                }
            } catch (NumberFormatException e) {
                throw new BadRequestException("Quantidade inválida");
            }
        }
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado"));
        inventoryService.removeItem(user, itemName, quantity);
        return ResponseEntity.ok().build();
    }
}
