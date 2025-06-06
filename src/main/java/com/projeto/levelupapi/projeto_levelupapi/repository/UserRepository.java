package com.projeto.levelupapi.projeto_levelupapi.repository;

import com.projeto.levelupapi.projeto_levelupapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Page<User> findAll(Pageable pageable);
}
