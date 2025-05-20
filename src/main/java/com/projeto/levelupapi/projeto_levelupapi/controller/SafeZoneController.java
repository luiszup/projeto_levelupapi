package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.service.UserService;
import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/safezone")
public class SafeZoneController {
    private final UserService userService;

    public SafeZoneController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{userId}/enter")
    public ResponseEntity<String> enterSafeZone(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setInSafeZone(true);
        userService.update(user.getId(), user);
        return ResponseEntity.ok("Você entrou na Zona de Segurança.");
    }

    @PostMapping("/{userId}/exit")
    public ResponseEntity<String> exitSafeZone(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setInSafeZone(false);
        userService.update(user.getId(), user);
        return ResponseEntity.ok("Você saiu da Zona de Segurança.");
    }

    @GetMapping("/{userId}/status")
    public ResponseEntity<Boolean> getSafeZoneStatus(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(user.isInSafeZone());
    }
}
