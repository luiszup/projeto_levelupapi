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

    public List<User> listarTodos() {
        return userRepository.findAll();
    }

    public Optional<User> buscarPorId(Long id) {
        return userRepository.findById(id);
    }

    public User criar(User user) {
        // Primeiro salvamos o usuário para obter o ID
        User savedUser = userRepository.save(user);
        
        // Depois criamos e associamos um registro Xp inicial
        Xp xpInicial = new Xp();
        xpInicial.setUser(savedUser);
        xpInicial.setXpPoints(0);
        xpInicial.setLevel(1);
        xpRepository.save(xpInicial);
        
        return savedUser;
    }

    public User atualizar(Long id, User novoUser) {
        return userRepository.findById(id).map(u -> {
            u.setUsername(novoUser.getUsername());
            u.setPassword(novoUser.getPassword());
            return userRepository.save(u);
        }).orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + id + " não encontrado"));
    }

    public void deletar(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário com ID " + id + " não encontrado");
        }
        userRepository.deleteById(id);
    }
}