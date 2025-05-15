package com.projeto.levelupapi.service;

import com.projeto.levelupapi.model.User;
import com.projeto.levelupapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> listarTodos() {
        return userRepository.findAll();
    }

    public Optional<User> buscarPorId(Long id) {
        return userRepository.findById(id);
    }

    public User criar(User user) {
        return userRepository.save(user);
    }

    public User atualizar(Long id, User novoUser) {
        return userRepository.findById(id).map(u -> {
            u.setUsername(novoUser.getUsername());
            u.setPassword(novoUser.getPassword());
            return userRepository.save(u);
        }).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public void deletar(Long id) {
        userRepository.deleteById(id);
    }
}
