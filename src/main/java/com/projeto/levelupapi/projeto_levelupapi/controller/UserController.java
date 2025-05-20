package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.dto.UserRequestDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.UserResponseDto;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userService.listAll(Pageable.unpaged()).stream().map(user -> {
            UserResponseDto dto = new UserResponseDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setLevel(user.getXpData() != null ? user.getXpData().getLevel() : 1);
            dto.setXp(user.getXpData() != null ? user.getXpData().getXpPoints() : 0);
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/paged")
    public Page<UserResponseDto> getAllUsersPaged(Pageable pageable) {
        return userService.listAll(pageable).map(user -> {
            UserResponseDto dto = new UserResponseDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setLevel(user.getXpData() != null ? user.getXpData().getLevel() : 1);
            dto.setXp(user.getXpData() != null ? user.getXpData().getXpPoints() : 0);
            return dto;
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> {
                    UserResponseDto dto = new UserResponseDto();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setLevel(user.getXpData() != null ? user.getXpData().getLevel() : 1);
                    dto.setXp(user.getXpData() != null ? user.getXpData().getXpPoints() : 0);
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        User user = new User();
        user.setUsername(userRequestDto.getUsername());
        user.setPassword(userRequestDto.getPassword());
        User novo = userService.create(user);
        UserResponseDto dto = new UserResponseDto();
        dto.setId(novo.getId());
        dto.setUsername(novo.getUsername());
        dto.setLevel(novo.getXpData() != null ? novo.getXpData().getLevel() : 1);
        dto.setXp(novo.getXpData() != null ? novo.getXpData().getXpPoints() : 0);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestDto userRequestDto) {
        User user = new User();
        user.setUsername(userRequestDto.getUsername());
        user.setPassword(userRequestDto.getPassword());
        User atualizado = userService.update(id, user);
        UserResponseDto dto = new UserResponseDto();
        dto.setId(atualizado.getId());
        dto.setUsername(atualizado.getUsername());
        dto.setLevel(atualizado.getXpData() != null ? atualizado.getXpData().getLevel() : 1);
        dto.setXp(atualizado.getXpData() != null ? atualizado.getXpData().getXpPoints() : 0);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}