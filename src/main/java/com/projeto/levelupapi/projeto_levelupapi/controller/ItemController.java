package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping
    public ResponseEntity<Item> criarItem (@RequestBody Map<String, String> body) {
        Item item = itemService.criarItem(
                body.get("name"),
                body.get("description")
        );
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public List<Item> listarTodos() {
        return itemService.listarTodos();
    }
}
