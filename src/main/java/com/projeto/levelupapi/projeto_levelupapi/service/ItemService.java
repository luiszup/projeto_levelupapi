package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public Item criarItem(String name, String description) {
        if (itemRepository.procurarPorNome(name).isPresent()) {
            throw new RuntimeException("O item já existe");
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
}
