package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceAlreadyExistsException;
import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.exception.BadRequestException;
import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.ArrayList;

@Service
public class ItemService {
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item createItem(String name, String description) {
        logger.info("Creating item: {}", name);
        if (itemRepository.findByName(name).isPresent()) {
            logger.warn("Item already exists: {}", name);
            throw new ResourceAlreadyExistsException("O item com nome '" + name + "' já existe");
        }
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        Item saved = itemRepository.save(item);
        logger.info("Item created successfully: {} (ID: {})", saved.getName(), saved.getId());
        return saved;
    }

    public List<Item> listAll() {
        logger.info("Listing all items");
        return itemRepository.findAll();
    }

    public Page<Item> listAll(Pageable pageable) {
        logger.info("Listing items with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return itemRepository.findAll(pageable);
    }

    public Optional<Item> findByName(String name) {
        return itemRepository.findByName(name);
    }

    public List<String> getAvailableItemsForLevel(int level) {
        // Sistema de desbloqueio por nível - expandido
        List<String> items = new ArrayList<>();
        
        // Itens básicos para iniciantes (nível 1)
        if (level >= 1) {
            items.addAll(Arrays.asList("Poção de Cura", "Pão Simples", "Adaga de Madeira"));
        }
        if (level >= 2) {
            items.addAll(Arrays.asList("Espada de Ferro", "Elmo de Couro"));
        }
        if (level >= 3) {
            items.addAll(Arrays.asList("Machado de Ferro", "Poção de Mana", "Armadura Reforçada"));
        }
        if (level >= 4) {
            items.addAll(Arrays.asList("Arco Longo", "Botas de Velocidade"));
        }
        if (level >= 5) {
            items.addAll(Arrays.asList("Espada de Prata", "Escudo Mágico", "Anel de Poder"));
        }
        if (level >= 7) {
            items.addAll(Arrays.asList("Armadura de Placas", "Poção Superior de Cura"));
        }
        if (level >= 10) {
            items.addAll(Arrays.asList("Espada Encantada", "Capa da Invisibilidade", "Amuleto da Sorte"));
        }
        if (level >= 15) {
            items.addAll(Arrays.asList("Machado dos Titãs", "Orbe de Fogo", "Botas Aladas"));
        }
        if (level >= 20) {
            items.addAll(Arrays.asList("Espada Lendária", "Armadura do Dragão", "Coroa do Rei"));
        }
        if (level >= 25) {
            items.addAll(Arrays.asList("Arma Divina", "Escudo Celestial", "Elixir da Vida Eterna"));
        }
        
        // Itens épicos para níveis altos
        if (level >= 50) {
            items.addAll(Arrays.asList("Espada do Caos", "Armadura do Tempo", "Anel da Realidade"));
        }
        if (level >= 75) {
            items.addAll(Arrays.asList("Lâmina do Infinito", "Manto das Estrelas"));
        }
        if (level >= 90) {
            items.addAll(Arrays.asList("Artefato Primordial"));
        }
        if (level >= 100) {
            items.addAll(Arrays.asList("Poder Absoluto", "Essência Divina"));
        }
        
        logger.info("Available items for level {}: {} items", level, items.size());
        return items;
    }
}