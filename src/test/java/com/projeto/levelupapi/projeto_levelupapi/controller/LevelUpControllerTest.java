package com.projeto.levelupapi.projeto_levelupapi.controller;

import com.projeto.levelupapi.projeto_levelupapi.model.User;
import com.projeto.levelupapi.projeto_levelupapi.model.Xp;
import com.projeto.levelupapi.projeto_levelupapi.service.ItemService;
import com.projeto.levelupapi.projeto_levelupapi.service.InventoryService;
import com.projeto.levelupapi.projeto_levelupapi.service.UserService;
import com.projeto.levelupapi.projeto_levelupapi.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do LevelUpController")
class LevelUpControllerTest {

    @Mock
    private ItemService itemService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private LevelUpController levelUpController;    @Test
    @DisplayName("Deve retornar itens disponíveis para usuário válido")
    void deveRetornarItensDisponiveisParaUsuarioValido() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("João");
        
        Xp xpData = new Xp();
        xpData.setLevel(5);
        user.setXpData(xpData);
        
        List<String> itensDisponiveis = Arrays.asList("Espada de Ferro", "Poção de Cura", "Escudo de Bronze");
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(itemService.getAvailableItemsForLevel(5)).thenReturn(itensDisponiveis);

        // Act
        ResponseEntity<List<String>> response = levelUpController.getAvailableItems(userId);

        // Assert
        assertAll("Verificações de itens disponíveis",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals(3, response.getBody().size()),
            () -> assertTrue(response.getBody().contains("Espada de Ferro")),
            () -> assertTrue(response.getBody().contains("Poção de Cura")),
            () -> assertTrue(response.getBody().contains("Escudo de Bronze"))
        );
        
        verify(userService).findById(userId);
        verify(itemService).getAvailableItemsForLevel(5);
    }    @Test
    @DisplayName("Deve retornar itens para usuário nível 1 quando xpData for null")
    void deveRetornarItensParaUsuarioNivel1QuandoXpDataForNull() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("Iniciante");
        user.setXpData(null); // XpData null = nível 1
        
        List<String> itensNivel1 = Arrays.asList("Poção de Cura", "Pão Simples", "Adaga de Madeira");
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(itemService.getAvailableItemsForLevel(1)).thenReturn(itensNivel1);

        // Act
        ResponseEntity<List<String>> response = levelUpController.getAvailableItems(userId);

        // Assert
        assertAll("Verificações para usuário nível 1",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals(3, response.getBody().size()),
            () -> assertTrue(response.getBody().contains("Poção de Cura")),
            () -> assertTrue(response.getBody().contains("Pão Simples")),
            () -> assertTrue(response.getBody().contains("Adaga de Madeira"))
        );
        
        verify(userService).findById(userId);
        verify(itemService).getAvailableItemsForLevel(1);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado para itens disponíveis")
    void deveLancarExcecaoQuandoUsuarioNaoForEncontradoParaItensDisponiveis() {
        // Arrange
        Long userId = 999L;
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> levelUpController.getAvailableItems(userId)
        );
        
        assertEquals("User not found", exception.getMessage());
        verify(userService).findById(userId);
        verify(itemService, never()).getAvailableItemsForLevel(anyInt());
    }

    @Test
    @DisplayName("Deve escolher item com sucesso quando usuário está na zona segura")
    void deveEscolherItemComSucessoQuandoUsuarioEstaNaZonaSegura() {
        // Arrange
        Long userId = 1L;
        String nomeItem = "Espada de Ferro";
        Map<String, String> body = Map.of("itemName", nomeItem);
          User user = new User();
        user.setId(userId);
        user.setUsername("João");
        user.setInSafeZone(true);
        
        Xp xpData = new Xp();
        xpData.setLevel(3);
        user.setXpData(xpData);
        
        List<String> itensDisponiveis = Arrays.asList("Espada de Ferro", "Poção de Cura", "Escudo de Bronze");
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(itemService.getAvailableItemsForLevel(3)).thenReturn(itensDisponiveis);

        // Act
        ResponseEntity<String> response = levelUpController.chooseItem(userId, body);

        // Assert
        assertAll("Verificações de escolha de item",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertNotNull(response.getBody()),
            () -> assertEquals("Item Espada de Ferro adicionado ao inventário!", response.getBody())
        );
        
        verify(userService).findById(userId);
        verify(itemService).getAvailableItemsForLevel(3);
        verify(inventoryService).addItem(user, nomeItem, 1);
    }

    @Test
    @DisplayName("Deve retornar erro quando nome do item não for fornecido")
    void deveRetornarErroQuandoNomeDoItemNaoForFornecido() {
        // Arrange
        Long userId = 1L;
        Map<String, String> body = new HashMap<>(); // Body sem itemName

        // Act
        ResponseEntity<String> response = levelUpController.chooseItem(userId, body);

        // Assert
        assertAll("Verificações de erro de nome obrigatório",
            () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
            () -> assertEquals("Nome do item é obrigatório", response.getBody())
        );
        
        verify(userService, never()).findById(any());
        verify(itemService, never()).getAvailableItemsForLevel(anyInt());
        verify(inventoryService, never()).addItem(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Deve retornar erro quando usuário não estiver na zona segura")
    void deveRetornarErroQuandoUsuarioNaoEstiverNaZonaSegura() {
        // Arrange
        Long userId = 1L;
        String nomeItem = "Espada de Ferro";
        Map<String, String> body = Map.of("itemName", nomeItem);
          User user = new User();
        user.setId(userId);
        user.setUsername("João");
        user.setInSafeZone(false); // Não está na zona segura
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<String> response = levelUpController.chooseItem(userId, body);

        // Assert
        assertAll("Verificações de erro de zona segura",
            () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
            () -> assertEquals("Você precisa estar na Zona de Segurança para escolher um item de level up.", response.getBody())
        );
        
        verify(userService).findById(userId);
        verify(itemService, never()).getAvailableItemsForLevel(anyInt());
        verify(inventoryService, never()).addItem(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Deve retornar erro quando item não estiver disponível para o nível")
    void deveRetornarErroQuandoItemNaoEstiverDisponivelParaONivel() {
        // Arrange
        Long userId = 1L;
        String nomeItem = "Espada Lendária"; // Item não disponível para nível baixo
        Map<String, String> body = Map.of("itemName", nomeItem);
        
        User user = new User();        user.setId(userId);
        user.setUsername("João");
        user.setInSafeZone(true);
        
        Xp xpData = new Xp();
        xpData.setLevel(2);
        user.setXpData(xpData);
        
        List<String> itensDisponiveis = Arrays.asList("Poção de Cura", "Pão Simples", "Adaga de Madeira");
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(itemService.getAvailableItemsForLevel(2)).thenReturn(itensDisponiveis);

        // Act
        ResponseEntity<String> response = levelUpController.chooseItem(userId, body);

        // Assert
        assertAll("Verificações de item indisponível",
            () -> assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()),
            () -> assertEquals("Item não disponível para o seu nível", response.getBody())
        );
        
        verify(userService).findById(userId);
        verify(itemService).getAvailableItemsForLevel(2);
        verify(inventoryService, never()).addItem(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado para escolha de item")
    void deveLancarExcecaoQuandoUsuarioNaoForEncontradoParaEscolhaDeItem() {
        // Arrange
        Long userId = 999L;
        Map<String, String> body = Map.of("itemName", "Qualquer Item");
        
        when(userService.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> levelUpController.chooseItem(userId, body)
        );
        
        assertEquals("User not found", exception.getMessage());
        verify(userService).findById(userId);
        verify(itemService, never()).getAvailableItemsForLevel(anyInt());
        verify(inventoryService, never()).addItem(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Deve escolher item com sucesso para usuário nível 1 com xpData null")
    void deveEscolherItemComSucessoParaUsuarioNivel1ComXpDataNull() {
        // Arrange
        Long userId = 1L;
        String nomeItem = "Poção de Cura";
        Map<String, String> body = Map.of("itemName", nomeItem);
          User user = new User();
        user.setId(userId);
        user.setUsername("Iniciante");
        user.setInSafeZone(true);
        user.setXpData(null); // Nível 1 por padrão
        
        List<String> itensNivel1 = Arrays.asList("Poção de Cura", "Pão Simples", "Adaga de Madeira");
        
        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(itemService.getAvailableItemsForLevel(1)).thenReturn(itensNivel1);

        // Act
        ResponseEntity<String> response = levelUpController.chooseItem(userId, body);

        // Assert
        assertAll("Verificações para usuário nível 1",
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals("Item Poção de Cura adicionado ao inventário!", response.getBody())
        );
        
        verify(userService).findById(userId);
        verify(itemService).getAvailableItemsForLevel(1);
        verify(inventoryService).addItem(user, nomeItem, 1);
    }
}
