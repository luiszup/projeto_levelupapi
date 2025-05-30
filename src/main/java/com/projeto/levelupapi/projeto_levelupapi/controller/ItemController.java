package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.service.ItemService;
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
@RequestMapping("/items")
@Tag(name = "⚔️ Itens do Sistema", description = "Gerenciamento dos itens disponíveis no sistema RPG")
@SecurityRequirement(name = "bearerAuth")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @Operation(
        summary = "Criar novo item",
        description = "Cria um novo item no sistema que pode ser usado como recompensa ou adicionado ao inventário dos jogadores"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos - nome e descrição são obrigatórios"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<Item> createItem(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do novo item",
            content = @Content(
                examples = @ExampleObject(
                    value = "{ \"name\": \"Espada de Ferro\", \"description\": \"Uma espada resistente forjada em ferro puro\" }"
                )
            )
        )
        @RequestBody Map<String, String> body) {
        Item item = itemService.createItem(
                body.get("name"),
                body.get("description")
        );
        return ResponseEntity.ok(item);
    }

    @GetMapping
    @Operation(
        summary = "Listar todos os itens",
        description = "Retorna uma lista completa de todos os itens disponíveis no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de itens retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public List<Item> getAllItems() {
        return itemService.listAll();
    }

    @GetMapping("/paged")
    @Operation(
        summary = "Listar itens com paginação",
        description = "Retorna uma lista paginada de itens para melhor performance em grandes volumes"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de itens retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public Page<Item> getAllItemsPaged(
        @Parameter(description = "Parâmetros de paginação (page, size, sort)")
        Pageable pageable) {
        return itemService.listAll(pageable);
    }
}