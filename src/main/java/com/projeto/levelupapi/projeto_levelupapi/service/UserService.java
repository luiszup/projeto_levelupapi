package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.repository.UserRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.XpRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository, XpRepository xpRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.xpRepository = xpRepository;
        this.passwordEncoder = passwordEncoder;
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
        
        logger.info("Usuário criado com sucesso: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
        return savedUser;
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