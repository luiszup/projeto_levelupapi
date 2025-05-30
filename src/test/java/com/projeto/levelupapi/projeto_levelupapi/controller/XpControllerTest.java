package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.service.XpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do XpController")
class XpControllerTest {

    @Mock
    private XpService xpService;

    @InjectMocks
    private XpController xpController;

    @Test
    @DisplayName("Deve adicionar XP com sucesso")
    void deveAdicionarXpComSucesso() {
        // Arrange
        Long userId = 1L;
        int xpGanho = 100;
        String mensagemEsperada = "XP adicionado com sucesso. XP atual: 150, Nível atual: 2";

        when(xpService.adicionarXp(userId, xpGanho)).thenReturn(mensagemEsperada);

        // Act
        ResponseEntity<String> response = xpController.adicionarXp(userId, xpGanho);

        // Assert
        assertAll("Verificações da adição de XP",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(mensagemEsperada, response.getBody())
        );
        
        verify(xpService).adicionarXp(userId, xpGanho);
    }

    @Test
    @DisplayName("Deve retornar mensagem de level up ao adicionar XP suficiente")
    void deveRetornarMensagemLevelUpAoAdicionarXpSuficiente() {
        // Arrange
        Long userId = 1L;
        int xpGanho = 500;
        String mensagemLevelUp = "Parabéns! Você subiu para o nível 3! Volte para a Zona de Segurança para escolher seu novo item.";

        when(xpService.adicionarXp(userId, xpGanho)).thenReturn(mensagemLevelUp);

        // Act
        ResponseEntity<String> response = xpController.adicionarXp(userId, xpGanho);

        // Assert
        assertAll("Verificações do level up",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(mensagemLevelUp, response.getBody())
        );
        
        verify(xpService).adicionarXp(userId, xpGanho);
    }

    @Test
    @DisplayName("Deve falhar ao adicionar XP para usuário inexistente")
    void deveFalharAoAdicionarXpParaUsuarioInexistente() {
        // Arrange
        Long userId = 999L;
        int xpGanho = 100;

        when(xpService.adicionarXp(userId, xpGanho))
            .thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> xpController.adicionarXp(userId, xpGanho)
        );
        
        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(xpService).adicionarXp(userId, xpGanho);
    }

    @Test
    @DisplayName("Deve obter XP atual do jogador com sucesso")
    void deveObterXpAtualDoJogadorComSucesso() {
        // Arrange
        Long userId = 1L;
        Xp xp = new Xp();
        xp.setXpPoints(250);
        xp.setLevel(3);

        when(xpService.obterXp(userId)).thenReturn(xp);

        // Act
        ResponseEntity<Integer> response = xpController.obterXp(userId);

        // Assert
        assertAll("Verificações da obtenção de XP",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(250, response.getBody())
        );
        
        verify(xpService).obterXp(userId);
    }

    @Test
    @DisplayName("Deve falhar ao obter XP de usuário inexistente")
    void deveFalharAoObterXpDeUsuarioInexistente() {
        // Arrange
        Long userId = 999L;

        when(xpService.obterXp(userId))
            .thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> xpController.obterXp(userId)
        );
        
        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(xpService).obterXp(userId);
    }

    @Test
    @DisplayName("Deve resetar XP com sucesso")
    void deveResetarXpComSucesso() {
        // Arrange
        Long userId = 1L;
        String mensagemEsperada = "XP resetado com sucesso. XP atual: 0, Nível atual: 1";

        when(xpService.resetXp(userId)).thenReturn(mensagemEsperada);

        // Act
        ResponseEntity<String> response = xpController.resetXp(userId);

        // Assert
        assertAll("Verificações do reset de XP",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(mensagemEsperada, response.getBody())
        );
        
        verify(xpService).resetXp(userId);
    }

    @Test
    @DisplayName("Deve falhar ao resetar XP de usuário inexistente")
    void deveFalharAoResetarXpDeUsuarioInexistente() {
        // Arrange
        Long userId = 999L;

        when(xpService.resetXp(userId))
            .thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> xpController.resetXp(userId)
        );
        
        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(xpService).resetXp(userId);
    }
}
