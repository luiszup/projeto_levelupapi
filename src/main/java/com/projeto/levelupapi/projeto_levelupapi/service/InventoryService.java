package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.exception.BadRequestException;
import com.projeto.levelupapi.projeto_levelupapi.model.InventoryItem;
import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.repository.InventoryItemRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryItemRepository inventoryRepository;
    private final ItemRepository itemRepository;

    public InventoryService(InventoryItemRepository inventoryRepository, ItemRepository itemRepository) {
        this.inventoryRepository = inventoryRepository;
        this.itemRepository = itemRepository;
    }

    public List<InventoryItem> getInventory(User user) {
        return inventoryRepository.findByUser(user);
    }

    public Page<InventoryItem> listInventory(User user, Pageable pageable) {
        logger.info("Listing inventory for user {} with pagination: page={}, size={}", user.getUsername(), pageable.getPageNumber(), pageable.getPageSize());
        return inventoryRepository.findByUser(user, pageable);
    }

    public InventoryItem addItem(User user, String itemName, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
        
        Item item = itemRepository.findByName(itemName)
                .orElseThrow(() -> new ResourceNotFoundException("Item with name '" + itemName + "' not found"));
                
        InventoryItem inventoryItem = inventoryRepository.findByUserAndItem(user, item)
                .orElse(new InventoryItem());
                
        inventoryItem.setUser(user);
        inventoryItem.setItem(item);
        inventoryItem.setQuantity(inventoryItem.getQuantity() + quantity);
        
        return inventoryRepository.save(inventoryItem);
    }

    public void removeItem(User user, String itemName, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity to remove must be greater than zero");
        }
        
        Item item = itemRepository.findByName(itemName)
                .orElseThrow(() -> new ResourceNotFoundException("Item with name '" + itemName + "' not found"));
                
        InventoryItem inventoryItem = inventoryRepository.findByUserAndItem(user, item)
                .orElseThrow(() -> new ResourceNotFoundException("Item '" + itemName + "' is not in the user's inventory"));
                
        int newQuantity = inventoryItem.getQuantity() - quantity;
        
        if (newQuantity <= 0) {
            inventoryRepository.delete(inventoryItem);
        } else {
            inventoryItem.setQuantity(newQuantity);
            inventoryRepository.save(inventoryItem);
        }
    }
}