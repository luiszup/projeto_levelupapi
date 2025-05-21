package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.repository.UserRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.XpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private XpRepository xpRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User create(User user) {
        // Primeiro salvamos o usuário para obter o ID
        User savedUser = userRepository.save(user);
        
        // Depois criamos e associamos um registro Xp inicial
        Xp initialXp = new Xp();
        initialXp.setUser(savedUser);
        initialXp.setXpPoints(0);
        initialXp.setLevel(1);
        xpRepository.save(initialXp);
        
        return savedUser;
    }

    public User update(Long id, User newUser) {
        return userRepository.findById(id).map(u -> {
            u.setUsername(newUser.getUsername());
            u.setPassword(newUser.getPassword());
            return userRepository.save(u);
        }).orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + id + " não encontrado"));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário com ID " + id + " não encontrado");
        }
        userRepository.deleteById(id);
    }
}