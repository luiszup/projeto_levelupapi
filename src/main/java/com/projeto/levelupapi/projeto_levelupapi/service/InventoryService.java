package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.model.InventoryItem;
import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.repository.InventoryItemRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private InventoryItemRepository inventoryRepository;
    private ItemRepository itemRepository;

    public InventoryService(InventoryItemRepository inventoryRepository, ItemRepository itemRepository) {
        this.inventoryRepository = inventoryRepository;
        this.itemRepository = itemRepository;
    }

    public List<InventoryItem> pegarInventario(User user) {
        return inventoryRepository.procurarPorJogador(user);
    }

    public InventoryItem adicionarItem(User user, String itemName, int quantity) {
        Item item = itemRepository.procurarPorNome(itemName)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));
        InventoryItem inventoryItem = inventoryRepository.procurarPorJogadorEItem(user, item)
                .orElse(new InventoryItem());
        inventoryItem.setUser(user);
        inventoryItem.setItem(item);
        inventoryItem.setQuantity(inventoryItem.getQuantity() + quantity);
        return inventoryRepository.save(inventoryItem);
    }

    public void removerItem(User user, String itemName, int quantity) {
        Item item = itemRepository.procurarPorNome(itemName)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));
        InventoryItem inventoryItem = inventoryRepository.procurarPorJogadorEItem(user, item)
                .orElseThrow(() -> new RuntimeException("Item não está no inventário"));
        int newQuantity = inventoryItem.getQuantity() - quantity;
        if (newQuantity <= 0) {
            inventoryRepository.delete(inventoryItem);
        } else {
            inventoryItem.setQuantity(newQuantity);
            inventoryRepository.save(inventoryItem);
        }
    }
}
