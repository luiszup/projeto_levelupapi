package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do SafeZoneController")
class SafeZoneControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private SafeZoneController safeZoneController;

    @Test
    @DisplayName("Deve entrar na zona de segurança com sucesso")
    void deveEntrarNaZonaDeSegurancaComSucesso() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("player1");
        user.setInSafeZone(false);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(userService.update(eq(userId), any(User.class))).thenReturn(user);

        // Act
        ResponseEntity<String> response = safeZoneController.enterSafeZone(userId);

        // Assert
        assertAll("Verificações da entrada na zona de segurança",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals("Você entrou na Zona de Segurança.", response.getBody())
        );
        
        verify(userService).findById(userId);
        verify(userService).update(eq(userId), any(User.class));
    }

    @Test
    @DisplayName("Deve sair da zona de segurança com sucesso")
    void deveSairDaZonaDeSegurancaComSucesso() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("player1");
        user.setInSafeZone(true);

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(userService.update(eq(userId), any(User.class))).thenReturn(user);

        // Act
        ResponseEntity<String> response = safeZoneController.exitSafeZone(userId);

        // Assert
        assertAll("Verificações da saída da zona de segurança",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals("Você saiu da Zona de Segurança.", response.getBody())
        );
        
        verify(userService).findById(userId);
        verify(userService).update(eq(userId), any(User.class));
    }

    @Test
    @DisplayName("Deve verificar status na zona de segurança quando usuário está dentro")
    void deveVerificarStatusNaZonaDeSegurancaQuandoUsuarioEstaDentro() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setInSafeZone(true);

        when(userService.findById(userId)).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<Boolean> response = safeZoneController.getSafeZoneStatus(userId);

        // Assert
        assertAll("Verificações do status na zona de segurança",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertTrue(response.getBody())
        );
        
        verify(userService).findById(userId);
    }

    @Test
    @DisplayName("Deve verificar status na zona de segurança quando usuário está fora")
    void deveVerificarStatusNaZonaDeSegurancaQuandoUsuarioEstaFora() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setInSafeZone(false);

        when(userService.findById(userId)).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<Boolean> response = safeZoneController.getSafeZoneStatus(userId);

        // Assert
        assertAll("Verificações do status fora da zona de segurança",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertFalse(response.getBody())
        );
        
        verify(userService).findById(userId);
    }

    @Test
    @DisplayName("Deve falhar ao entrar na zona de segurança com usuário inexistente")
    void deveFalharAoEntrarNaZonaDeSegurancaComUsuarioInexistente() {
        // Arrange
        Long userId = 999L;
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> safeZoneController.enterSafeZone(userId)
        );
        
        assertEquals("User not found", exception.getMessage());
        verify(userService).findById(userId);
        verify(userService, never()).update(anyLong(), any(User.class));
    }

    @Test
    @DisplayName("Deve falhar ao sair da zona de segurança com usuário inexistente")
    void deveFalharAoSairDaZonaDeSegurancaComUsuarioInexistente() {
        // Arrange
        Long userId = 999L;
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> safeZoneController.exitSafeZone(userId)
        );
        
        assertEquals("User not found", exception.getMessage());
        verify(userService).findById(userId);
        verify(userService, never()).update(anyLong(), any(User.class));
    }

    @Test
    @DisplayName("Deve falhar ao verificar status com usuário inexistente")
    void deveFalharAoVerificarStatusComUsuarioInexistente() {
        // Arrange
        Long userId = 999L;
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> safeZoneController.getSafeZoneStatus(userId)
        );
        
        assertEquals("User not found", exception.getMessage());
        verify(userService).findById(userId);
    }
}
