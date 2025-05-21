package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.exception.BadRequestException;
import com.projeto.levelupapi.projeto_levelupapi.model.InventoryItem;
import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.repository.InventoryItemRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {
    private final InventoryItemRepository inventoryRepository;
    private final ItemRepository itemRepository;

    public InventoryService(InventoryItemRepository inventoryRepository, ItemRepository itemRepository) {
        this.inventoryRepository = inventoryRepository;
        this.itemRepository = itemRepository;
    }

    public List<InventoryItem> getInventory(User user) {
        return inventoryRepository.findByUser(user);
    }

    public InventoryItem addItem(User user, String itemName, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("A quantidade deve ser maior que zero");
        }
        
        Item item = itemRepository.findByName(itemName)
                .orElseThrow(() -> new ResourceNotFoundException("Item com nome '" + itemName + "' não encontrado"));
                
        InventoryItem inventoryItem = inventoryRepository.findByUserAndItem(user, item)
                .orElse(new InventoryItem());
                
        inventoryItem.setUser(user);
        inventoryItem.setItem(item);
        inventoryItem.setQuantity(inventoryItem.getQuantity() + quantity);
        
        return inventoryRepository.save(inventoryItem);
    }

    public void removeItem(User user, String itemName, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("A quantidade a remover deve ser maior que zero");
        }
        
        Item item = itemRepository.findByName(itemName)
                .orElseThrow(() -> new ResourceNotFoundException("Item com nome '" + itemName + "' não encontrado"));
                
        InventoryItem inventoryItem = inventoryRepository.findByUserAndItem(user, item)
                .orElseThrow(() -> new ResourceNotFoundException("Item '" + itemName + "' não está no inventário do usuário"));
                
        int newQuantity = inventoryItem.getQuantity() - quantity;
        
        if (newQuantity <= 0) {
            inventoryRepository.delete(inventoryItem);
        } else {
            inventoryItem.setQuantity(newQuantity);
            inventoryRepository.save(inventoryItem);
        }
    }
}