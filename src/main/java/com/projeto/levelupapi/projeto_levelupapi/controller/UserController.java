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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "👥 Usuários", description = "Gerenciamento de jogadores do sistema RPG")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar todos os jogadores",
        description = "Retorna uma lista completa de todos os jogadores cadastrados no sistema, " +
                     "incluindo informações de nível e XP atual"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de jogadores retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Listar jogadores com paginação",
        description = "Retorna uma lista paginada de jogadores para melhor performance em grandes volumes de dados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de jogadores retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public Page<UserResponseDto> getAllUsersPaged(
        @Parameter(description = "Parâmetros de paginação (page, size, sort)")
        Pageable pageable) {
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Buscar jogador por ID",
        description = "Retorna as informações detalhadas de um jogador específico, incluindo nível e XP atual"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jogador encontrado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<UserResponseDto> getUserById(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long id) {
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
    @Operation(
        summary = "Criar novo jogador",
        description = "⭐ ENDPOINT PÚBLICO - Cadastra um novo jogador no sistema. Não requer autenticação. " +
                     "O jogador inicia no nível 1 com 0 XP."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jogador criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos - verifique username e password"),
        @ApiResponse(responseCode = "409", description = "Username já existe no sistema")
    })
    public ResponseEntity<UserResponseDto> createUser(
        @Valid @RequestBody UserRequestDto userRequestDto) {
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Atualizar dados do jogador",
        description = "Atualiza as informações de um jogador existente. XP e nível são mantidos"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Jogador atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<UserResponseDto> updateUser(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long id, 
        @Valid @RequestBody UserRequestDto userRequestDto) {
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Remover jogador",
        description = "Remove permanentemente um jogador do sistema. ATENÇÃO: Esta ação não pode ser desfeita!"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Jogador removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Jogador não encontrado"),
        @ApiResponse(responseCode = "401", description = "Token de autenticação inválido ou ausente")
    })
    public ResponseEntity<Void> deleteUser(
        @Parameter(description = "ID único do jogador", required = true)
        @PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}