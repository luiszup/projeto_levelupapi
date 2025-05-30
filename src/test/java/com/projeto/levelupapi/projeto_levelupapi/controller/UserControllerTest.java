package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.dto.UserRequestDto;
import com.projeto.levelupapi.projeto_levelupapi.dto.UserResponseDto;
import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.Role;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("Deve retornar lista de usuários com status 200")
    void shouldReturnUserList() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("player1");
        user1.setRole(Role.USER);
        
        Xp xp1 = new Xp();
        xp1.setLevel(5);
        xp1.setXpPoints(150);
        user1.setXpData(xp1);
        
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("admin");
        user2.setRole(Role.ADMIN);

        List<User> users = Arrays.asList(user1, user2);
        when(userService.listAll()).thenReturn(users);

        // Act
        List<UserResponseDto> response = userController.getAllUsers();

        // Assert
        assertAll(
            () -> assertNotNull(response, "A resposta não deve ser nula"),
            () -> assertEquals(2, response.size(), "Devem ser retornados 2 usuários"),
            () -> assertEquals("player1", response.get(0).getUsername()),
            () -> assertEquals(5, response.get(0).getLevel()),
            () -> assertEquals(150, response.get(0).getXp()),
            () -> assertEquals("admin", response.get(1).getUsername())
        );
        
        verify(userService).listAll();
    }

    @Test
    @DisplayName("Deve retornar usuários com paginação")
    void shouldReturnUsersWithPagination() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("player1");
        
        List<User> users = Arrays.asList(user1);
        Page<User> userPage = new PageImpl<>(users);
        Pageable pageable = PageRequest.of(0, 10);        when(userService.listAll(pageable)).thenReturn(userPage);

        // Act
        Page<UserResponseDto> response = userController.getAllUsersPaged(pageable);

        // Assert
        assertAll(
            () -> assertNotNull(response, "A resposta não deve ser nula"),
            () -> assertEquals(1, response.getContent().size(), "Deve retornar 1 usuário"),
            () -> assertEquals("player1", response.getContent().get(0).getUsername())
        );
        
        verify(userService).listAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar usuário por ID com status 200")
    void shouldReturnUserById() {        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("testplayer");
        user.setRole(Role.USER);
        
        Xp xpData = new Xp();
        xpData.setLevel(3);
        xpData.setXpPoints(75);
        user.setXpData(xpData);
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<UserResponseDto> response = userController.getUserById(userId);

        // Assert
        assertAll(
            () -> assertNotNull(response, "A resposta não deve ser nula"),
            () -> assertNotNull(response.getBody(), "O corpo da resposta não deve ser nulo"),
            () -> assertEquals(HttpStatus.OK, response.getStatusCode(), "Status HTTP deve ser 200 OK"),
            () -> assertEquals(userId, response.getBody().getId()),
            () -> assertEquals("testplayer", response.getBody().getUsername()),
            () -> assertEquals(3, response.getBody().getLevel()),
            () -> assertEquals(75, response.getBody().getXp())
        );
        
        verify(userService).findById(userId);
    }    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não é encontrado")
    void shouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {        // Arrange
        Long userId = 999L;
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserResponseDto> response = userController.getUserById(userId);
        
        // Assert
        assertEquals(404, response.getStatusCodeValue(), "Deve retornar status 404 quando usuário não existir");
        assertNull(response.getBody(), "Body deve ser null quando usuário não é encontrado");
        
        verify(userService).findById(userId);
    }

    @Test
    @DisplayName("Deve excluir usuário e retornar status 204")
    void shouldDeleteUserAndReturn204Status() {
        // Arrange
        Long userId = 1L;
        doNothing().when(userService).delete(userId);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(userId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "Status HTTP deve ser 204 NO CONTENT");
        assertNull(response.getBody(), "O corpo da resposta deve ser nulo");
        
        verify(userService).delete(userId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando excluir usuário inexistente")
    void shouldHandleExceptionWhenDeletingNonExistentUser() {
        // Arrange
        Long userId = 999L;
        doThrow(new ResourceNotFoundException("Usuário não encontrado com ID: " + userId))
            .when(userService).delete(userId);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userController.deleteUser(userId);
        }, "Deve lançar ResourceNotFoundException para usuário inexistente");
        
        verify(userService).delete(userId);
    }    @Test
    @DisplayName("Deve retornar UserResponseDto com dados corretos quando usuário não tem XP")
    void shouldReturnUserResponseDtoWithDefaultXpWhenUserHasNoXpData() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("newplayer");
        user.setRole(Role.USER);
        user.setXpData(null); // Sem dados de XP
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<UserResponseDto> response = userController.getUserById(userId);

        // Assert
        assertAll(
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("newplayer", response.getBody().getUsername()),
            () -> assertEquals(1, response.getBody().getLevel(), "Nível padrão deve ser 1"),
            () -> assertEquals(0, response.getBody().getXp(), "XP padrão deve ser 0")
        );
    }

    @Test
    @DisplayName("Deve tratar lista vazia de usuários corretamente")
    void shouldHandleEmptyUserListCorrectly() {
        // Arrange
        when(userService.listAll()).thenReturn(Arrays.asList());

        // Act
        List<UserResponseDto> response = userController.getAllUsers();

        // Assert
        assertAll(
            () -> assertNotNull(response, "A resposta não deve ser nula"),
            () -> assertTrue(response.isEmpty(), "A lista deve estar vazia")        );
        
        verify(userService).listAll();
    }

    @Test
    @DisplayName("Deve mapear corretamente dados do usuário para UserResponseDto")
    void shouldMapUserDataToUserResponseDtoCorrectly() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(Role.ADMIN);
        user.setInSafeZone(true);
        
        Xp xpData = new Xp();
        xpData.setLevel(10);
        xpData.setXpPoints(500);
        user.setXpData(xpData);

        when(userService.findById(1L)).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<UserResponseDto> response = userController.getUserById(1L);
        UserResponseDto dto = response.getBody();
        
        // Assert
        assertAll(
            () -> assertEquals(1L, dto.getId()),
            () -> assertEquals("testuser", dto.getUsername()),
            () -> assertEquals(10, dto.getLevel()),
            () -> assertEquals(500, dto.getXp())
        );
    }

    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void deveDeletarUsuarioComSucesso() {
        // Arrange
        Long userId = 1L;
        doNothing().when(userService).delete(userId);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(userId);

        // Assert
        assertAll("Verificações da deleção",
            () -> assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode()),
            () -> assertNull(response.getBody())
        );
        
        verify(userService).delete(userId);
    }

    @Test
    @DisplayName("Deve propagar exceção quando tentar deletar usuário inexistente")
    void devePropagarExcecaoQuandoTentarDeletarUsuarioInexistente() {
        // Arrange
        Long userId = 999L;
        doThrow(new ResourceNotFoundException("User with ID 999 not found"))
            .when(userService).delete(userId);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userController.deleteUser(userId)
        );
        
        assertEquals("User with ID 999 not found", exception.getMessage());
        verify(userService).delete(userId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há usuários")
    void deveRetornarListaVaziaQuandoNaoHaUsuarios() {
        // Arrange
        when(userService.listAll()).thenReturn(Arrays.asList());

        // Act
        List<UserResponseDto> response = userController.getAllUsers();

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(userService).listAll();
    }

    @Test
    @DisplayName("Deve listar usuários paginados quando há usuários")
    void deveListarUsuariosPaginadosQuandoHaUsuarios() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("player1");
        
        Xp xp1 = new Xp();
        xp1.setLevel(3);
        xp1.setXpPoints(80);
        user1.setXpData(xp1);
        
        List<User> users = Arrays.asList(user1);
        Page<User> pagedUsers = new PageImpl<>(users, pageable, 1);
        
        when(userService.listAll(pageable)).thenReturn(pagedUsers);

        // Act
        Page<UserResponseDto> response = userController.getAllUsersPaged(pageable);

        // Assert
        assertAll("Verificações da paginação",
            () -> assertNotNull(response),
            () -> assertEquals(1, response.getContent().size()),
            () -> assertEquals(0, response.getNumber()),
            () -> assertEquals(10, response.getSize()),
            () -> assertEquals(1, response.getTotalElements()),
            () -> assertEquals("player1", response.getContent().get(0).getUsername()),
            () -> assertEquals(3, response.getContent().get(0).getLevel()),
            () -> assertEquals(80, response.getContent().get(0).getXp())
        );
        
        verify(userService).listAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há usuários para paginação")
    void deveRetornarPaginaVaziaQuandoNaoHaUsuariosParaPaginacao() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(userService.listAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<UserResponseDto> response = userController.getAllUsersPaged(pageable);

        // Assert
        assertAll("Verificações da página vazia",
            () -> assertNotNull(response),
            () -> assertTrue(response.getContent().isEmpty()),
            () -> assertEquals(0, response.getTotalElements()),
            () -> assertEquals(0, response.getTotalPages())
        );
        
        verify(userService).listAll(pageable);
    }

    @Test
    @DisplayName("Deve lidar com usuário sem dados de XP na listagem")
    void deveLidarComUsuarioSemDadosDeXpNaListagem() {
        // Arrange
        User userSemXp = new User();
        userSemXp.setId(1L);
        userSemXp.setUsername("playerSemXp");
        userSemXp.setXpData(null); // Sem dados de XP
        
        when(userService.listAll()).thenReturn(Arrays.asList(userSemXp));

        // Act
        List<UserResponseDto> response = userController.getAllUsers();

        // Assert
        assertAll("Verificações de usuário sem XP",
            () -> assertNotNull(response),
            () -> assertEquals(1, response.size()),
            () -> assertEquals("playerSemXp", response.get(0).getUsername()),
            () -> assertEquals(1, response.get(0).getLevel()), // Default level
            () -> assertEquals(0, response.get(0).getXp()) // Default XP
        );
        
        verify(userService).listAll();
    }

    @Test
    @DisplayName("Deve lidar com usuário sem dados de XP no update")
    void deveLidarComUsuarioSemDadosDeXpNoUpdate() {
        // Arrange
        Long userId = 1L;
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setUsername("updatedUser");
        requestDto.setPassword("newPassword");
        
        User userAtualizado = new User();
        userAtualizado.setId(userId);
        userAtualizado.setUsername("updatedUser");
        userAtualizado.setXpData(null); // Sem dados de XP
        
        when(userService.update(eq(userId), any(User.class))).thenReturn(userAtualizado);

        // Act
        ResponseEntity<UserResponseDto> response = userController.updateUser(userId, requestDto);

        // Assert
        assertAll("Verificações do update sem XP",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("updatedUser", response.getBody().getUsername()),
            () -> assertEquals(1, response.getBody().getLevel()), // Default level
            () -> assertEquals(0, response.getBody().getXp()) // Default XP
        );
        
        verify(userService).update(eq(userId), any(User.class));
    }

    @Test
    @DisplayName("Deve lidar com usuário sem dados de XP na criação")
    void deveLidarComUsuarioSemDadosDeXpNaCriacao() {
        // Arrange
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setUsername("newUser");
        requestDto.setPassword("password");
        
        User userCriado = new User();
        userCriado.setId(1L);
        userCriado.setUsername("newUser");
        userCriado.setXpData(null); // Sem dados de XP
        
        when(userService.create(any(User.class))).thenReturn(userCriado);

        // Act
        ResponseEntity<UserResponseDto> response = userController.createUser(requestDto);

        // Assert
        assertAll("Verificações da criação sem XP",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("newUser", response.getBody().getUsername()),
            () -> assertEquals(1, response.getBody().getLevel()), // Default level
            () -> assertEquals(0, response.getBody().getXp()) // Default XP
        );
        
        verify(userService).create(any(User.class));
    }
}
