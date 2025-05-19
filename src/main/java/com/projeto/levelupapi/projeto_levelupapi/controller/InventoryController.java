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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserService userService;

    @Autowired
    public InventoryController(InventoryService inventoryService, UserService userService) {
        this.inventoryService = inventoryService;
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<InventoryItem>> obterInventario(@PathVariable Long userId) {
        User user = userService.buscarPorId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado"));
        
        List<InventoryItem> inventory = inventoryService.pegarInventario(user);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/{userId}/add")
    public ResponseEntity<InventoryItem> adicionarItem(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {
        
        // Validações de entrada
        if (!body.containsKey("itemName") || body.get("itemName") == null) {
            throw new BadRequestException("Nome do item é obrigatório");
        }
        
        String itemName = body.get("itemName").toString();
        int quantity = 1;  // Valor padrão
        
        // Tente obter a quantidade, se fornecida
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
        
        User user = userService.buscarPorId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado"));
        
        InventoryItem item = inventoryService.adicionarItem(user, itemName, quantity);
        return ResponseEntity.ok(item);
    }

    @PostMapping("/{userId}/remove")
    public ResponseEntity<Void> removerItem(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {
        
        // Validações de entrada
        if (!body.containsKey("itemName") || body.get("itemName") == null) {
            throw new BadRequestException("Nome do item é obrigatório");
        }
        
        String itemName = body.get("itemName").toString();
        int quantity = 1;  // Valor padrão
        
        // Tente obter a quantidade, se fornecida
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
        
        User user = userService.buscarPorId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado"));
        
        inventoryService.removerItem(user, itemName, quantity);
        return ResponseEntity.ok().build();
    }
}