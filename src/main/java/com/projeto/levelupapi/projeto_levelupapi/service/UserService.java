package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.model.InventoryItem;
import com.projeto.levelupapi.projeto_levelupapi.model.Item;
import com.projeto.levelupapi.projeto_levelupapi.repository.UserRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.XpRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.ItemRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final XpRepository xpRepository;
    private final PasswordEncoder passwordEncoder;
    private final InventoryService inventoryService;
    private final ItemRepository itemRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, XpRepository xpRepository, PasswordEncoder passwordEncoder, InventoryService inventoryService, ItemRepository itemRepository) {
        this.userRepository = userRepository;
        this.xpRepository = xpRepository;
        this.passwordEncoder = passwordEncoder;
        this.inventoryService = inventoryService;
        this.itemRepository = itemRepository;
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }

    public Page<User> listAll(Pageable pageable) {
        logger.info("Listing users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User create(User user) {
        logger.info("Creating new user: {}", user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        
        Xp xpInicial = new Xp();
        xpInicial.setUser(savedUser);
        xpInicial.setXpPoints(0);
        xpInicial.setLevel(1);
        xpRepository.save(xpInicial);
        
        addInitialItems(savedUser);
        
        logger.info("User created successfully: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    private void addInitialItems(User user) {
        addItemToInventory(user, "Mapa do Jogo", "Ajuda a navegar pelas zonas do jogo", 1);
        addItemToInventory(user, "Poção de Cura", "Recupera HP", 3);
        addItemToInventory(user, "Espada de Madeira", "Arma básica para início de combate", 1);
        addItemToInventory(user, "Escudo de Couro", "Proteção básica para defesa", 1);
    }

    private void addItemToInventory(User user, String name, String description, int quantity) {
        Item item = itemRepository.findByName(name).orElseGet(() -> {
            Item novo = new Item();
            novo.setName(name);
            novo.setDescription(description);
            return itemRepository.save(novo);
        });
        inventoryService.addItem(user, name, quantity);
    }

    public User update(Long id, User newUser) {
        return userRepository.findById(id).map(u -> {
            u.setUsername(newUser.getUsername());
            if (!newUser.getPassword().equals(u.getPassword())) {
                u.setPassword(passwordEncoder.encode(newUser.getPassword()));
            }
            return userRepository.save(u);
        }).orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found"));
    }

    public void delete(Long id) {
        logger.warn("Trying to delete user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            logger.error("User with ID {} not found for deletion", id);
            throw new ResourceNotFoundException("User with ID " + id + " not found");
        }
        userRepository.deleteById(id);
        logger.info("User deleted successfully: {}", id);
    }
}