package com.projeto.levelupapi.projeto_levelupapi.repository;

import com.projeto.levelupapi.projeto_levelupapi.model.InventoryItem;
import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByUser(User user);
    Optional<InventoryItem> findByUserAndItem(User user, Item item);
    Page<InventoryItem> findByUser(User user, Pageable pageable);
}
