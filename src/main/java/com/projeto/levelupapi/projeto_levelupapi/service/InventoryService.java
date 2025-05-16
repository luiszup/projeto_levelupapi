package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.model.InventoryItem;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.repository.InventoryItemRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryItemRepository inventoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    public List<InventoryItem> pegarInventario(User user) {
        return inventoryRepository.procurarPorJogador(user);
    }
}
