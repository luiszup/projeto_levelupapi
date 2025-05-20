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
    
    public Item buscarPorNomeObrigatorio(String name) {
        return itemRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Item com nome '" + name + "' não encontrado"));
    }

    public List<String> getAvailableItemsForLevel(int level) {
        // Exemplo de desbloqueio por nível
        if (level == 2) {
            return Arrays.asList("Espada de Ferro", "Elmo de Couro");
        } else if (level == 3) {
            return Arrays.asList("Machado de Ferro", "Poção de Mana", "Armadura Reforçada");
        } else if (level == 4) {
            return Arrays.asList("Arco Longo", "Botas de Velocidade");
        }
        // ...adicionar mais conforme necessidade...
        return Arrays.asList();
    }
}