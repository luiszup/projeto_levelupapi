package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.exception.ResourceAlreadyExistsException;
import com.projeto.levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item criarItem(String name, String description) {
        if (itemRepository.procurarPorNome(name).isPresent()) {
            throw new ResourceAlreadyExistsException("O item com nome '" + name + "' já existe");
        }
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        return itemRepository.save(item);
    }

    public List<Item> listarTodos() {
        return itemRepository.findAll();
    }

    public Optional<Item> procurarPorNome(String name) {
        return itemRepository.procurarPorNome(name);
    }
    
    public Item buscarPorNomeObrigatorio(String name) {
        return itemRepository.procurarPorNome(name)
                .orElseThrow(() -> new ResourceNotFoundException("Item com nome '" + name + "' não encontrado"));
    }
}