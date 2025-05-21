package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.service.ItemService;
import com.projeto.levelupapi.projeto_levelupapi.service.InventoryService;
import com.projeto.levelupapi.projeto_levelupapi.service.UserService;
import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/levelup")
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
    public ResponseEntity<List<String>> getAvailableItems(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        int level = user.getXpData() != null ? user.getXpData().getLevel() : 1;
        return ResponseEntity.ok(itemService.getAvailableItemsForLevel(level));
    }

    @PostMapping("/{userId}/choose-item")
    public ResponseEntity<String> chooseItem(@PathVariable Long userId, @RequestBody Map<String, String> body) {
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
