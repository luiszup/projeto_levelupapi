package com.projeto.levelupapi.projeto_levelupapi.service;

import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.repository.UserRepository;
import com.projeto.levelupapi.projeto_levelupapi.repository.XpRepository;
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

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do XpService")
class XpServiceTest {

    @Mock
    private XpRepository xpRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private XpService xpService;    @Test
    @DisplayName("Deve adicionar XP com sucesso sem level up")
    void deveAdicionarXpComSucessoSemLevelUp() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("player1");

        Xp xp = new Xp();
        xp.setUser(user);
        xp.setXpPoints(50);
        xp.setLevel(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(xpRepository.findByUserId(userId)).thenReturn(Optional.of(xp));
        when(xpRepository.save(any(Xp.class))).thenReturn(xp);

        // Act - Adiciona apenas 30 XP para não fazer level up (50 + 30 = 80, precisa de 100 para level up)
        String resultado = xpService.adicionarXp(userId, 30);

        // Assert
        assertTrue(resultado.contains("XP adicionado com sucesso"));
        verify(userRepository).findById(userId);
        verify(xpRepository).findByUserId(userId);
        verify(xpRepository).save(xp);
    }

    @Test
    @DisplayName("Deve adicionar XP com level up")
    void deveAdicionarXpComLevelUp() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("player1");

        Xp xp = new Xp();
        xp.setUser(user);
        xp.setXpPoints(50);
        xp.setLevel(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(xpRepository.findByUserId(userId)).thenReturn(Optional.of(xp));
        when(xpRepository.save(any(Xp.class))).thenReturn(xp);

        // Act - Adiciona 100 XP para fazer level up (50 + 100 = 150, precisa de 100 para level 2)
        String resultado = xpService.adicionarXp(userId, 100);

        // Assert
        assertTrue(resultado.contains("Parabéns! Você subiu para o nível"));
        verify(userRepository).findById(userId);
        verify(xpRepository).findByUserId(userId);
        verify(xpRepository).save(xp);
    }

    @Test
    @DisplayName("Deve retornar mensagem de level up ao subir de nível")
    void deveRetornarMensagemLevelUpAoSubirDeNivel() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("player1");

        Xp xp = new Xp();
        xp.setUser(user);
        xp.setXpPoints(90);
        xp.setLevel(1);

        // Simular que o XP será suficiente para subir de nível
        Xp xpUpdated = new Xp();
        xpUpdated.setUser(user);
        xpUpdated.setXpPoints(190);
        xpUpdated.setLevel(2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(xpRepository.findByUserId(userId)).thenReturn(Optional.of(xp));
        when(xpRepository.save(any(Xp.class))).thenAnswer(invocation -> {
            Xp savedXp = invocation.getArgument(0);
            savedXp.addXp(100); // Simular a adição de XP que causa level up
            return savedXp;
        });

        // Act
        String resultado = xpService.adicionarXp(userId, 100);

        // Assert
        assertTrue(resultado.contains("Parabéns! Você subiu para o nível"));
        verify(userRepository).findById(userId);
        verify(xpRepository).save(any(Xp.class));
    }

    @Test
    @DisplayName("Deve limitar nível máximo a 100")
    void deveLimitarNivelMaximoA100() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("player1");

        Xp xp = new Xp();
        xp.setUser(user);
        xp.setXpPoints(1000);
        xp.setLevel(100);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(xpRepository.findByUserId(userId)).thenReturn(Optional.of(xp));

        // Act
        String resultado = xpService.adicionarXp(userId, 100);

        // Assert
        assertEquals("Você já atingiu o nível máximo (100). Não é possível ganhar mais XP.", resultado);
        verify(userRepository).findById(userId);
        verify(xpRepository, never()).save(any(Xp.class));
    }

    @Test
    @DisplayName("Deve criar novo XP quando usuário não tem registro")
    void deveCriarNovoXpQuandoUsuarioNaoTemRegistro() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("player1");

        Xp newXp = new Xp();
        newXp.setUser(user);
        newXp.setXpPoints(0);
        newXp.setLevel(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(xpRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(xpRepository.save(any(Xp.class))).thenReturn(newXp);

        // Act
        String resultado = xpService.adicionarXp(userId, 50);

        // Assert
        assertTrue(resultado.contains("XP adicionado com sucesso"));
        verify(xpRepository, times(2)).save(any(Xp.class)); // Uma vez para criar, outra para atualizar
    }

    @Test
    @DisplayName("Deve falhar ao adicionar XP para usuário inexistente")
    void deveFalharAoAdicionarXpParaUsuarioInexistente() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> xpService.adicionarXp(userId, 100)
        );

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(xpRepository, never()).save(any(Xp.class));
    }

    @Test
    @DisplayName("Deve obter XP existente com sucesso")
    void deveObterXpExistenteComSucesso() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Xp xp = new Xp();
        xp.setUser(user);
        xp.setXpPoints(250);
        xp.setLevel(3);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(xpRepository.findByUserId(userId)).thenReturn(Optional.of(xp));

        // Act
        Xp resultado = xpService.obterXp(userId);

        // Assert
        assertNotNull(resultado);
        assertEquals(250, resultado.getXpPoints());
        assertEquals(3, resultado.getLevel());
        verify(userRepository).findById(userId);
        verify(xpRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Deve criar XP se não existir ao obter")
    void deveCriarXpSeNaoExistirAoObter() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Xp newXp = new Xp();
        newXp.setUser(user);
        newXp.setXpPoints(0);
        newXp.setLevel(1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(xpRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(xpRepository.save(any(Xp.class))).thenReturn(newXp);

        // Act
        Xp resultado = xpService.obterXp(userId);

        // Assert
        assertNotNull(resultado);
        assertEquals(0, resultado.getXpPoints());
        assertEquals(1, resultado.getLevel());
        verify(xpRepository).save(any(Xp.class));
    }

    @Test
    @DisplayName("Deve obter nível atual do jogador")
    void deveObterNivelAtualDoJogador() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Xp xp = new Xp();
        xp.setUser(user);
        xp.setLevel(5);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(xpRepository.findByUserId(userId)).thenReturn(Optional.of(xp));

        // Act
        int nivel = xpService.obterNivel(userId);

        // Assert
        assertEquals(5, nivel);
    }

    @Test
    @DisplayName("Deve obter pontos XP atuais do jogador")
    void deveObterPontosXpAtuaisDoJogador() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Xp xp = new Xp();
        xp.setUser(user);
        xp.setXpPoints(350);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(xpRepository.findByUserId(userId)).thenReturn(Optional.of(xp));

        // Act
        int pontosXp = xpService.obterPontosXp(userId);

        // Assert
        assertEquals(350, pontosXp);
    }

    @Test
    @DisplayName("Deve listar todos os XP com paginação")
    void deveListarTodosOsXpComPaginacao() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Xp xp1 = new Xp();
        Xp xp2 = new Xp();
        Page<Xp> page = new PageImpl<>(Arrays.asList(xp1, xp2), pageable, 2);

        when(xpRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Xp> resultado = xpService.listAll(pageable);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        assertEquals(2, resultado.getTotalElements());
        verify(xpRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve resetar XP com sucesso")
    void deveResetarXpComSucesso() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("player1");

        Xp xp = new Xp();
        xp.setUser(user);
        xp.setXpPoints(500);
        xp.setLevel(5);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(xpRepository.findByUserId(userId)).thenReturn(Optional.of(xp));
        when(xpRepository.save(any(Xp.class))).thenReturn(xp);

        // Act
        String resultado = xpService.resetXp(userId);

        // Assert
        assertEquals("XP resetado com sucesso. XP atual: 0, Nível atual: 1", resultado);
        assertEquals(0, xp.getXpPoints());
        assertEquals(1, xp.getLevel());
        verify(userRepository).findById(userId);
        verify(xpRepository).save(xp);
    }

    @Test
    @DisplayName("Deve falhar ao resetar XP de usuário inexistente")
    void deveFalharAoResetarXpDeUsuarioInexistente() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> xpService.resetXp(userId)
        );

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(xpRepository, never()).save(any(Xp.class));
    }
}
