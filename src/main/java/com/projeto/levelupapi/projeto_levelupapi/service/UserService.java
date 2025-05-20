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

    public List<User> listarTodos() {
        return userRepository.findAll();
    }

    public Page<User> listAll(Pageable pageable) {
        logger.info("Listing users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    public Optional<User> buscarPorId(Long id) {
        return userRepository.findById(id);
    }

    public User criar(User user) {
        logger.info("Criando novo usuário: {}", user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        
        Xp xpInicial = new Xp();
        xpInicial.setUser(savedUser);
        xpInicial.setXpPoints(0);
        xpInicial.setLevel(1);
        xpRepository.save(xpInicial);
        
        adicionarItensIniciais(savedUser);
        
        logger.info("Usuário criado com sucesso: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    private void adicionarItensIniciais(User user) {
        adicionarItemAoInventario(user, "Mapa do Jogo", "Ajuda a navegar pelas zonas do jogo", 1);
        adicionarItemAoInventario(user, "Poção de Cura", "Recupera HP", 3);
        adicionarItemAoInventario(user, "Espada de Madeira", "Arma básica para início de combate", 1);
        adicionarItemAoInventario(user, "Escudo de Couro", "Proteção básica para defesa", 1);
    }

    private void adicionarItemAoInventario(User user, String nome, String descricao, int quantidade) {
        Item item = itemRepository.findByName(nome).orElseGet(() -> {
            Item novo = new Item();
            novo.setName(nome);
            novo.setDescription(descricao);
            return itemRepository.save(novo);
        });
        inventoryService.adicionarItem(user, nome, quantidade);
    }

    public User atualizar(Long id, User novoUser) {
        return userRepository.findById(id).map(u -> {
            u.setUsername(novoUser.getUsername());
            if (!novoUser.getPassword().equals(u.getPassword())) {
                u.setPassword(passwordEncoder.encode(novoUser.getPassword()));
            }
            return userRepository.save(u);
        }).orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + id + " não encontrado"));
    }

    public void deletar(Long id) {
        logger.warn("Tentando deletar usuário com ID: {}", id);
        if (!userRepository.existsById(id)) {
            logger.error("Usuário com ID {} não encontrado para deleção", id);
            throw new ResourceNotFoundException("Usuário com ID " + id + " não encontrado");
        }
        userRepository.deleteById(id);
        logger.info("Usuário deletado com sucesso: {}", id);
    }
}